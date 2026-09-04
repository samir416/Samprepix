package com.aiinterview.backend;

import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.repository.CodingProblemRepository;
import com.aiinterview.backend.repository.CodingTestCaseRepository;
import com.aiinterview.backend.service.coding.SqlProblemGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SqlProblemImporterTest {

    @Autowired
    private CodingProblemRepository codingProblemRepository;

    @Autowired
    private CodingTestCaseRepository codingTestCaseRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void importAndVerifyAllSqlProblems() throws Exception {
        System.out.println("=== Starting SQL Problems Generation & Sandbox Verification ===");

        List<SqlProblemGenerator.GeneratedProblem> generatedProblems =
                SqlProblemGenerator.generateAllProblems();

        assertEquals(1200, generatedProblems.size(), "Must generate exactly 1,200 SQL problems");

        String sandboxUrl = "jdbc:mysql://localhost:3306/ai_platform_sandbox?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&allowMultiQueries=true";
        String sandboxUser = "sql_sandbox_user";
        String sandboxPass = "SandboxPass123!";

        String mainUrl = "jdbc:mysql://localhost:3306/ai_platform?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";
        String mainUser = "root";
        String mainPass = "SAMIR";

        // Check if SQL problems are already imported
        try (Connection mainConn = DriverManager.getConnection(mainUrl, mainUser, mainPass)) {
            try (Statement stmt = mainConn.createStatement();
                 ResultSet rs = stmt.executeQuery("SELECT count(*) FROM coding_problems WHERE source_id LIKE 'sql-%'")) {
                if (rs.next() && rs.getInt(1) >= 1200) {
                    System.out.println("All 1,200 SQL problems already exist in the database! Verifying count...");
                    verifyDatabaseState(mainConn);
                    return;
                }
            }
        }

        // Map to hold ground truth outputs: (problemSourceId_testCaseNumber -> jsonExpectedOutput)
        Map<String, String> expectedOutputs = new HashMap<>(2400);

        System.out.println("Evaluating reference queries in MySQL Sandbox to obtain verified ground-truth JSON outputs...");
        long evalStart = System.currentTimeMillis();

        try (Connection sandboxConn = DriverManager.getConnection(sandboxUrl, sandboxUser, sandboxPass)) {
            try (Statement stmt = sandboxConn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            }

            int count = 0;
            for (SqlProblemGenerator.GeneratedProblem prob : generatedProblems) {
                for (SqlProblemGenerator.GeneratedTestCase tc : prob.testCases()) {
                    String expectedJson = evaluateInSandbox(sandboxConn, tc.inputDdlDml(), tc.referenceQuery());
                    expectedOutputs.put(prob.sourceId() + "_" + tc.testCaseNumber(), expectedJson);
                }
                count++;
                if (count % 200 == 0) {
                    System.out.printf("Sandbox verified %d/1200 problems (%d test cases)...%n", count, count * 2);
                }
            }
            try (Statement stmt = sandboxConn.createStatement()) {
                stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
            }
        }

        System.out.printf("Sandbox evaluation complete in %d ms! Now batch inserting 1,200 problems into ai_platform...%n",
                (System.currentTimeMillis() - evalStart));

        // Batch insert into ai_platform
        long insertStart = System.currentTimeMillis();
        try (Connection mainConn = DriverManager.getConnection(mainUrl, mainUser, mainPass)) {
            mainConn.setAutoCommit(false);

            String insertProblemSql = """
                    INSERT INTO coding_problems 
                    (source_id, slug, title, difficulty, category, description, input_example, output_example, 
                     starter_code, language_configurations, minimum_experience_level, active) 
                    VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, 1)
                    """;

            String insertTagSql = "INSERT INTO coding_problem_tags (problem_id, tag) VALUES (?, ?)";
            String insertConstraintSql = "INSERT INTO coding_problem_constraints (problem_id, constraint_text) VALUES (?, ?)";
            String insertTestCaseSql = """
                    INSERT INTO coding_test_cases 
                    (problem_id, test_case_number, hidden, input, expected_output, active) 
                    VALUES (?, ?, ?, ?, ?, 1)
                    """;

            try (PreparedStatement probStmt = mainConn.prepareStatement(insertProblemSql, Statement.RETURN_GENERATED_KEYS);
                 PreparedStatement tagStmt = mainConn.prepareStatement(insertTagSql);
                 PreparedStatement constrStmt = mainConn.prepareStatement(insertConstraintSql);
                 PreparedStatement tcStmt = mainConn.prepareStatement(insertTestCaseSql)) {

                int batchSize = 0;
                for (SqlProblemGenerator.GeneratedProblem prob : generatedProblems) {
                    probStmt.setString(1, prob.sourceId());
                    probStmt.setString(2, prob.slug());
                    probStmt.setString(3, prob.title());
                    probStmt.setString(4, prob.difficulty());
                    probStmt.setString(5, prob.category());
                    probStmt.setString(6, prob.description());
                    probStmt.setString(7, prob.inputExample());
                    probStmt.setString(8, prob.outputExample());
                    probStmt.setString(9, prob.starterCode());
                    probStmt.setString(10, prob.languageConfigurations());
                    probStmt.setInt(11, prob.minimumExperienceLevel());
                    probStmt.executeUpdate();

                    long generatedProblemId;
                    try (ResultSet keys = probStmt.getGeneratedKeys()) {
                        if (keys.next()) {
                            generatedProblemId = keys.getLong(1);
                        } else {
                            throw new IllegalStateException("Failed to retrieve generated ID for " + prob.sourceId());
                        }
                    }

                    // Tags
                    for (String tag : prob.tags()) {
                        tagStmt.setLong(1, generatedProblemId);
                        tagStmt.setString(2, tag);
                        tagStmt.addBatch();
                    }

                    // Constraints
                    for (String c : prob.constraints()) {
                        constrStmt.setLong(1, generatedProblemId);
                        constrStmt.setString(2, c);
                        constrStmt.addBatch();
                    }

                    // Test cases
                    for (SqlProblemGenerator.GeneratedTestCase tc : prob.testCases()) {
                        String key = prob.sourceId() + "_" + tc.testCaseNumber();
                        String expectedJson = expectedOutputs.getOrDefault(key, "[]");

                        tcStmt.setLong(1, generatedProblemId);
                        tcStmt.setInt(2, tc.testCaseNumber());
                        tcStmt.setBoolean(3, tc.hidden());
                        tcStmt.setString(4, tc.inputDdlDml());
                        tcStmt.setString(5, expectedJson);
                        tcStmt.addBatch();
                    }

                    batchSize++;
                    if (batchSize % 100 == 0) {
                        tagStmt.executeBatch();
                        constrStmt.executeBatch();
                        tcStmt.executeBatch();
                        mainConn.commit();
                        System.out.printf("Committed batch: %d/1200 problems...%n", batchSize);
                    }
                }

                tagStmt.executeBatch();
                constrStmt.executeBatch();
                tcStmt.executeBatch();
                mainConn.commit();
            }
        }

        System.out.printf("Batch insert complete in %d ms! Verifying database state...%n",
                (System.currentTimeMillis() - insertStart));

        try (Connection mainConn = DriverManager.getConnection(mainUrl, mainUser, mainPass)) {
            verifyDatabaseState(mainConn);
        }
    }

    private String evaluateInSandbox(Connection conn, String ddlDml, String query) throws SQLException {
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            try (ResultSet rs = stmt.executeQuery("SHOW TABLES;")) {
                List<String> tables = new ArrayList<>();
                while (rs.next()) {
                    tables.add(rs.getString(1));
                }
                for (String t : tables) {
                    stmt.execute("DROP TABLE IF EXISTS `" + t + "`;");
                }
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");

            for (String part : ddlDml.split(";")) {
                String trimmed = part.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }

            // Execute reference query
            try (ResultSet rs = stmt.executeQuery(query)) {
                List<Map<String, Object>> rows = new ArrayList<>();
                ResultSetMetaData md = rs.getMetaData();
                int colCount = md.getColumnCount();

                while (rs.next()) {
                    Map<String, Object> row = new LinkedHashMap<>();
                    for (int i = 1; i <= colCount; i++) {
                        String label = md.getColumnLabel(i).toLowerCase(Locale.ROOT);
                        Object val = rs.getObject(i);
                        if (val instanceof BigDecimal bd) {
                            row.put(label, bd.stripTrailingZeros().toPlainString());
                        } else if (val instanceof Number) {
                            row.put(label, val.toString());
                        } else if (val == null) {
                            row.put(label, null);
                        } else {
                            row.put(label, val.toString().trim());
                        }
                    }
                    rows.add(row);
                }
                return objectMapper.writeValueAsString(rows);
            } catch (Exception e) {
                throw new SQLException("Failed to serialize result set to JSON: " + e.getMessage(), e);
            }
        }
    }

    private void verifyDatabaseState(Connection mainConn) throws SQLException {
        try (Statement stmt = mainConn.createStatement()) {
            // Total Problems
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM coding_problems")) {
                assertTrue(rs.next());
                int total = rs.getInt(1);
                System.out.println("TOTAL PROBLEMS IN DATABASE: " + total);
                assertTrue(total >= 6260, "Total problems must be at least 6,260 (5,060 DSA + 1,200 SQL)");
            }

            // SQL Problems
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM coding_problems WHERE category = 'DATABASE'")) {
                assertTrue(rs.next());
                int sqlCount = rs.getInt(1);
                System.out.println("DATABASE/SQL PROBLEMS COUNT: " + sqlCount);
                assertEquals(1200, sqlCount, "Must have exactly 1,200 DATABASE problems");
            }

            // DSA Problems
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM coding_problems WHERE category = 'DSA'")) {
                assertTrue(rs.next());
                int dsaCount = rs.getInt(1);
                System.out.println("DSA PROBLEMS COUNT: " + dsaCount);
                assertEquals(5060, dsaCount, "DSA problems must remain exactly 5,060");
            }

            // Difficulties of SQL problems
            try (ResultSet rs = stmt.executeQuery("SELECT difficulty, count(*) FROM coding_problems WHERE category = 'DATABASE' GROUP BY difficulty")) {
                while (rs.next()) {
                    System.out.printf("SQL Difficulty %-8s: %d%n", rs.getString(1), rs.getInt(2));
                }
            }

            // Total Test Cases
            try (ResultSet rs = stmt.executeQuery("SELECT count(*) FROM coding_test_cases")) {
                assertTrue(rs.next());
                int tcTotal = rs.getInt(1);
                System.out.println("TOTAL TEST CASES IN DATABASE: " + tcTotal);
                assertTrue(tcTotal >= 22640, "Total test cases must be at least 22,640 (20,240 DSA + 2,400 SQL)");
            }
        }
    }
}
