package com.aiinterview.backend.service.coding;

import com.aiinterview.backend.dto.coding.CodeExecutionResponse;
import com.aiinterview.backend.dto.coding.CodeExecutionTestCaseResponse;
import com.aiinterview.backend.entity.CodingProblem;
import com.aiinterview.backend.entity.CodingTestCase;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Dedicated, isolated SQL execution and evaluation service.
 * Executes user SQL queries against the isolated 'ai_platform_sandbox' database
 * using the restricted 'sql_sandbox_user'. Zero access to production 'ai_platform'.
 */
@Service
public class DatabaseExecutionService {

    private static final Logger log = LoggerFactory.getLogger(DatabaseExecutionService.class);

    private static final Pattern FORBIDDEN_PATTERN = Pattern.compile(
            "\\b(DROP\\s+DATABASE|ALTER\\s+DATABASE|CREATE\\s+DATABASE|USE\\s+|GRANT\\s+|REVOKE\\s+|" +
            "LOAD\\s+DATA|INTO\\s+OUTFILE|INTO\\s+DUMPFILE|SHUTDOWN|SYSTEM|SLEEP\\s*\\(|BENCHMARK\\s*\\(|" +
            "ai_platform|information_schema|performance_schema|mysql)\\b",
            Pattern.CASE_INSENSITIVE
    );

    private static final Pattern TABLE_EXTRACT_PATTERN = Pattern.compile(
            "CREATE\\s+TABLE\\s+(?:IF\\s+NOT\\s+EXISTS\\s+)?`?([a-zA-Z0-9_]+)`?",
            Pattern.CASE_INSENSITIVE
    );

    private final String sandboxUrl;
    private final String sandboxUsername;
    private final String sandboxPassword;
    private final ObjectMapper objectMapper;
    private final Object executionLock = new Object();

    public DatabaseExecutionService(
            @Value("${coding.sandbox.mysql.url:jdbc:mysql://localhost:3306/ai_platform_sandbox?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC}")
            String sandboxUrl,
            @Value("${coding.sandbox.mysql.username:sql_sandbox_user}")
            String sandboxUsername,
            @Value("${coding.sandbox.mysql.password:SandboxPass123!}")
            String sandboxPassword
    ) {
        this.sandboxUrl = sandboxUrl;
        this.sandboxUsername = sandboxUsername;
        this.sandboxPassword = sandboxPassword;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Checks whether the given problem or language specifies SQL/Database execution.
     */
    public boolean isDatabaseProblem(CodingProblem problem, String language) {
        if (language != null) {
            String norm = language.trim().toLowerCase(Locale.ROOT);
            if ("mysql".equals(norm) || "sql".equals(norm)) {
                return true;
            }
        }
        if (problem != null) {
            if ("DATABASE".equalsIgnoreCase(problem.getCategory())) {
                return true;
            }
            if (problem.getTags() != null) {
                for (String tag : problem.getTags()) {
                    if ("Database".equalsIgnoreCase(tag) || "SQL".equalsIgnoreCase(tag) || "MySQL".equalsIgnoreCase(tag)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Validates the security and structure of the submitted SQL query.
     */
    public void validateSqlQuery(String sql) {
        if (sql == null || sql.isBlank()) {
            throw new IllegalArgumentException("SQL query cannot be empty.");
        }

        String trimmed = sql.trim();

        // Check for forbidden security patterns
        Matcher forbiddenMatcher = FORBIDDEN_PATTERN.matcher(trimmed);
        if (forbiddenMatcher.find()) {
            throw new SecurityException("Forbidden SQL statement or keyword detected: " + forbiddenMatcher.group());
        }

        // Clean trailing semicolon for statement validation
        String singleQuery = trimmed;
        while (singleQuery.endsWith(";")) {
            singleQuery = singleQuery.substring(0, singleQuery.length() - 1).trim();
        }

        // Check if there are multiple statements separated by semicolon
        // Allow semicolons only if inside quoted strings
        if (containsMultipleStatements(singleQuery)) {
            throw new SecurityException("Multiple SQL statements are not permitted in a single submission.");
        }

        // Must start with SELECT or WITH (CTE)
        String upper = singleQuery.toUpperCase(Locale.ROOT);
        if (!upper.startsWith("SELECT") && !upper.startsWith("WITH") && !upper.startsWith("SHOW") && !upper.startsWith("EXPLAIN")) {
            throw new SecurityException("Only SELECT or WITH (Common Table Expression) queries are permitted.");
        }
    }

    private boolean containsMultipleStatements(String query) {
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        for (int i = 0; i < query.length(); i++) {
            char c = query.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
            } else if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                return true;
            }
        }
        return false;
    }

    /**
     * Executes the user query against test cases in the isolated MySQL sandbox.
     */
    public CodeExecutionResponse execute(
            CodingProblem problem,
            List<CodingTestCase> testCases,
            String userQuery,
            boolean isSubmit
    ) {
        long startTime = System.currentTimeMillis();

        try {
            validateSqlQuery(userQuery);
        } catch (SecurityException se) {
            return CodeExecutionResponse.builder()
                    .status("SECURITY_VIOLATION")
                    .passed(false)
                    .error(se.getMessage())
                    .message("Security violation: " + se.getMessage())
                    .passedTests(0)
                    .totalTests(testCases.size())
                    .build();
        } catch (IllegalArgumentException iae) {
            return CodeExecutionResponse.builder()
                    .status("EXECUTION_ERROR")
                    .passed(false)
                    .error(iae.getMessage())
                    .message("Validation error: " + iae.getMessage())
                    .passedTests(0)
                    .totalTests(testCases.size())
                    .build();
        }

        String cleanQuery = userQuery.trim();
        while (cleanQuery.endsWith(";")) {
            cleanQuery = cleanQuery.substring(0, cleanQuery.length() - 1).trim();
        }

        List<CodeExecutionTestCaseResponse> testCaseResponses = new ArrayList<>();
        int passedCount = 0;
        boolean allPassed = true;
        String firstError = null;

        // Synchronize sandbox execution to avoid table collisions across concurrent test runs
        synchronized (executionLock) {
            try (Connection conn = DriverManager.getConnection(sandboxUrl, sandboxUsername, sandboxPassword)) {

                for (CodingTestCase testCase : testCases) {
                    CodeExecutionTestCaseResponse tcResponse = executeTestCase(conn, testCase, cleanQuery, isSubmit);
                    testCaseResponses.add(tcResponse);

                    if (tcResponse.isPassed()) {
                        passedCount++;
                    } else {
                        allPassed = false;
                        if (firstError == null && tcResponse.getActualOutput() != null) {
                            firstError = "Test Case " + testCase.getTestCaseNumber() + " failed";
                        }
                    }
                }

            } catch (SQLException e) {
                log.error("Database execution failed: {}", e.getMessage(), e);
                return CodeExecutionResponse.builder()
                        .status("DATABASE_ERROR")
                        .passed(false)
                        .error("Database error during execution: " + e.getMessage())
                        .message("Execution error: " + e.getMessage())
                        .passedTests(passedCount)
                        .totalTests(testCases.size())
                        .build();
            }
        }

        long totalTime = System.currentTimeMillis() - startTime;
        String status = allPassed ? "ACCEPTED" : "WRONG_ANSWER";
        String message = allPassed
                ? "All " + testCases.size() + " test cases passed!"
                : passedCount + "/" + testCases.size() + " test cases passed.";

        return CodeExecutionResponse.builder()
                .status(status)
                .passed(allPassed)
                .error(firstError)
                .message(message)
                .passedTests(passedCount)
                .totalTests(testCases.size())
                .runtime(totalTime)
                .testCases(testCaseResponses)
                .build();
    }

    private CodeExecutionTestCaseResponse executeTestCase(
            Connection conn,
            CodingTestCase testCase,
            String cleanQuery,
            boolean isSubmit
    ) {
        long tcStart = System.currentTimeMillis();
        List<String> createdTables = extractTableNames(testCase.getInput());

        // 1. Drop existing tables if any
        dropTables(conn, createdTables);

        // 2. Execute setup DDL & DML
        try {
            executeSetupScript(conn, testCase.getInput());
        } catch (SQLException e) {
            dropTables(conn, createdTables);
            return CodeExecutionTestCaseResponse.builder()
                    .testCaseNumber(testCase.getTestCaseNumber())
                    .passed(false)
                    .status("SETUP_ERROR")
                    .input(isSubmit && testCase.isHidden() ? null : testCase.getInput())
                    .expectedOutput(isSubmit && testCase.isHidden() ? null : testCase.getExpectedOutput())
                    .actualOutput(isSubmit && testCase.isHidden() ? null : "Setup Error: " + e.getMessage())
                    .build();
        }

        // 3. Execute user query
        List<Map<String, Object>> actualRows;
        try (Statement stmt = conn.createStatement()) {
            stmt.setQueryTimeout(5); // 5 seconds max query timeout
            stmt.setMaxRows(1000);   // Max 1000 rows

            try (ResultSet rs = stmt.executeQuery(cleanQuery)) {
                actualRows = extractResultSetRows(rs);
            }
        } catch (SQLException e) {
            dropTables(conn, createdTables);
            return CodeExecutionTestCaseResponse.builder()
                    .testCaseNumber(testCase.getTestCaseNumber())
                    .passed(false)
                    .status("SQL_ERROR")
                    .input(isSubmit && testCase.isHidden() ? null : testCase.getInput())
                    .expectedOutput(isSubmit && testCase.isHidden() ? null : testCase.getExpectedOutput())
                    .actualOutput(isSubmit && testCase.isHidden() ? null : "SQL Error: " + e.getMessage())
                    .build();
        }

        // 4. Cleanup sandbox tables
        dropTables(conn, createdTables);

        long tcDuration = System.currentTimeMillis() - tcStart;

        // 5. Compare actual vs expected
        boolean passed = compareResults(actualRows, testCase.getExpectedOutput(), cleanQuery);

        String actualJson;
        try {
            actualJson = objectMapper.writeValueAsString(actualRows);
        } catch (Exception e) {
            actualJson = actualRows.toString();
        }

        return CodeExecutionTestCaseResponse.builder()
                .testCaseNumber(testCase.getTestCaseNumber())
                .passed(passed)
                .status(passed ? "PASSED" : "FAILED")
                .runtime(tcDuration)
                .input(isSubmit && testCase.isHidden() ? null : testCase.getInput())
                .expectedOutput(isSubmit && testCase.isHidden() ? null : testCase.getExpectedOutput())
                .actualOutput(isSubmit && testCase.isHidden() ? null : actualJson)
                .build();
    }

    private List<String> extractTableNames(String script) {
        List<String> tables = new ArrayList<>();
        if (script == null) return tables;

        Matcher matcher = TABLE_EXTRACT_PATTERN.matcher(script);
        while (matcher.find()) {
            tables.add(matcher.group(1));
        }
        return tables;
    }

    private void dropTables(Connection conn, List<String> tables) {
        if (tables == null || tables.isEmpty()) return;
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("SET FOREIGN_KEY_CHECKS = 0;");
            for (String table : tables) {
                stmt.execute("DROP TABLE IF EXISTS `" + table + "`;");
            }
            stmt.execute("SET FOREIGN_KEY_CHECKS = 1;");
        } catch (SQLException ignored) {
            // Cleanup suppression
        }
    }

    private void executeSetupScript(Connection conn, String script) throws SQLException {
        if (script == null || script.isBlank()) return;

        List<String> statements = splitSqlStatements(script);
        try (Statement stmt = conn.createStatement()) {
            for (String sql : statements) {
                String trimmed = sql.trim();
                if (!trimmed.isEmpty()) {
                    stmt.execute(trimmed);
                }
            }
        }
    }

    private List<String> splitSqlStatements(String script) {
        List<String> statements = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;

        for (int i = 0; i < script.length(); i++) {
            char c = script.charAt(i);
            if (c == '\'' && !inDoubleQuote) {
                inSingleQuote = !inSingleQuote;
                current.append(c);
            } else if (c == '"' && !inSingleQuote) {
                inDoubleQuote = !inDoubleQuote;
                current.append(c);
            } else if (c == ';' && !inSingleQuote && !inDoubleQuote) {
                statements.add(current.toString());
                current.setLength(0);
            } else {
                current.append(c);
            }
        }
        if (current.length() > 0 && !current.toString().trim().isEmpty()) {
            statements.add(current.toString());
        }
        return statements;
    }

    private List<Map<String, Object>> extractResultSetRows(ResultSet rs) throws SQLException {
        List<Map<String, Object>> rows = new ArrayList<>();
        ResultSetMetaData md = rs.getMetaData();
        int columnCount = md.getColumnCount();

        while (rs.next()) {
            Map<String, Object> row = new LinkedHashMap<>();
            for (int i = 1; i <= columnCount; i++) {
                String colLabel = md.getColumnLabel(i);
                Object val = rs.getObject(i);
                row.put(colLabel.toLowerCase(Locale.ROOT), normalizeValue(val));
            }
            rows.add(row);
        }
        return rows;
    }

    private Object normalizeValue(Object val) {
        if (val == null) return null;
        if (val instanceof BigDecimal bd) {
            return bd.stripTrailingZeros().toPlainString();
        }
        if (val instanceof Number num) {
            if (num instanceof Double || num instanceof Float) {
                return BigDecimal.valueOf(num.doubleValue()).stripTrailingZeros().toPlainString();
            }
            return num.toString();
        }
        if (val instanceof byte[] bytes) {
            return new String(bytes);
        }
        return val.toString().trim();
    }

    private boolean compareResults(
            List<Map<String, Object>> actual,
            String expectedJson,
            String userQuery
    ) {
        if (expectedJson == null) return false;

        List<Map<String, Object>> expectedRows;
        try {
            expectedRows = objectMapper.readValue(
                    expectedJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );
        } catch (Exception e) {
            return false;
        }

        if (actual.size() != expectedRows.size()) {
            return false;
        }

        List<Map<String, Object>> normExpected = new ArrayList<>();
        for (Map<String, Object> r : expectedRows) {
            Map<String, Object> normRow = new LinkedHashMap<>();
            for (Map.Entry<String, Object> entry : r.entrySet()) {
                normRow.put(entry.getKey().toLowerCase(Locale.ROOT), normalizeValue(entry.getValue()));
            }
            normExpected.add(normRow);
        }

        boolean requiresOrder = userQuery.toUpperCase(Locale.ROOT).contains("ORDER BY");

        if (requiresOrder) {
            for (int i = 0; i < actual.size(); i++) {
                if (!compareRow(actual.get(i), normExpected.get(i))) {
                    return false;
                }
            }
            return true;
        } else {
            List<Map<String, Object>> remainingExpected = new ArrayList<>(normExpected);
            for (Map<String, Object> actRow : actual) {
                boolean matched = false;
                for (int i = 0; i < remainingExpected.size(); i++) {
                    if (compareRow(actRow, remainingExpected.get(i))) {
                        remainingExpected.remove(i);
                        matched = true;
                        break;
                    }
                }
                if (!matched) {
                    return false;
                }
            }
            return remainingExpected.isEmpty();
        }
    }

    private boolean compareRow(Map<String, Object> row1, Map<String, Object> row2) {
        if (row1.size() != row2.size()) return false;
        for (Map.Entry<String, Object> entry : row1.entrySet()) {
            String key = entry.getKey();
            if (!row2.containsKey(key)) return false;
            Object v1 = entry.getValue();
            Object v2 = row2.get(key);
            if (!Objects.equals(v1, v2)) {
                return false;
            }
        }
        return true;
    }
}
