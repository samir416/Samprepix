package com.aiinterview.backend.service.coding;

import java.util.*;

/**
 * High-performance, deterministic generator for 1,200 production-quality
 * SQL/Database interview problems covering 15 realistic industry domains
 * and 48 SQL algorithmic & analytical query patterns.
 */
public class SqlProblemGenerator {

    public record GeneratedTestCase(
            int testCaseNumber,
            boolean hidden,
            String inputDdlDml,
            String referenceQuery
    ) {}

    public record GeneratedProblem(
            String sourceId,
            String slug,
            String title,
            String difficulty,
            String category,
            List<String> tags,
            List<String> constraints,
            String description,
            String inputExample,
            String outputExample,
            String starterCode,
            String languageConfigurations,
            int minimumExperienceLevel,
            List<GeneratedTestCase> testCases
    ) {}

    private static final String[] DOMAINS = {
            "hr", "ecommerce", "rideshare", "social", "banking",
            "healthcare", "education", "streaming", "gaming", "realestate",
            "saas", "supplychain", "helpdesk", "marketing", "telemetry"
    };

    private static final String STARTER_CODE = """
            -- Write your MySQL query statement below
            SELECT
                *
            FROM
                ;
            """;

    private static final String LANG_CONFIG = """
            {"mysql":{"name":"mysql","displayName":"MySQL","runtimeLanguage":"mysql","runtimeVersion":"8.0","monacoLanguage":"sql","fileExtension":".sql","fileName":"solution.sql","icon":"database","executionMode":"database"}}
            """;

    /**
     * Generates all 1,200 SQL problems:
     * - 360 EASY (sql-0001 to sql-0360)
     * - 600 MEDIUM (sql-0361 to sql-0960)
     * - 240 HARD (sql-0961 to sql-1200)
     */
    public static List<GeneratedProblem> generateAllProblems() {
        List<GeneratedProblem> problems = new ArrayList<>(1200);

        // 1. Easy Problems (360)
        for (int i = 1; i <= 360; i++) {
            problems.add(generateEasyProblem(i));
        }

        // 2. Medium Problems (600)
        for (int i = 361; i <= 960; i++) {
            problems.add(generateMediumProblem(i));
        }

        // 3. Hard Problems (240)
        for (int i = 961; i <= 1200; i++) {
            problems.add(generateHardProblem(i));
        }

        return problems;
    }

    private static GeneratedProblem generateEasyProblem(int id) {
        String sourceId = String.format("sql-%04d", id);
        int domainIdx = (id - 1) % DOMAINS.length;
        String domain = DOMAINS[domainIdx];
        int patternIdx = ((id - 1) / DOMAINS.length) % 12;
        int variant = ((id - 1) / (DOMAINS.length * 12)) + 1;

        String title = buildTitle("EASY", domain, patternIdx, variant, id);
        String slug = buildSlug(sourceId, title);
        String tableName = getDomainTable(domain);

        String ddl = getDomainDdl(domain, tableName);
        String dmlPublic = getDomainDml(domain, tableName, false, variant);
        String dmlHidden = getDomainDml(domain, tableName, true, variant);

        String refQuery = getEasyQuery(domain, tableName, patternIdx, variant);
        String desc = buildDescription(title, "EASY", domain, ddl, dmlPublic, refQuery);

        List<GeneratedTestCase> testCases = List.of(
                new GeneratedTestCase(1, false, ddl + "\n" + dmlPublic, refQuery),
                new GeneratedTestCase(2, true, ddl + "\n" + dmlHidden, refQuery)
        );

        List<String> tags = List.of("Database", "SQL", "MySQL", capitalize(domain), getEasyTagName(patternIdx));
        List<String> constraints = List.of(
                "Table contains between 1 and 1000 rows.",
                "Column IDs are unique primary keys."
        );

        return new GeneratedProblem(
                sourceId,
                slug,
                title,
                "EASY",
                "DATABASE",
                tags,
                constraints,
                desc,
                ddl + "\n" + dmlPublic,
                "Result set ordered according to problem specification.",
                STARTER_CODE,
                LANG_CONFIG,
                0,
                testCases
        );
    }

    private static GeneratedProblem generateMediumProblem(int id) {
        String sourceId = String.format("sql-%04d", id);
        int offset = id - 361;
        int domainIdx = offset % DOMAINS.length;
        String domain = DOMAINS[domainIdx];
        int patternIdx = (offset / DOMAINS.length) % 20;
        int variant = (offset / (DOMAINS.length * 20)) + 1;

        String title = buildTitle("MEDIUM", domain, patternIdx, variant, id);
        String slug = buildSlug(sourceId, title);
        String tableName = getDomainTable(domain);
        String secondaryTable = getDomainSecondaryTable(domain);

        String ddl = getDomainMultiDdl(domain, tableName, secondaryTable);
        String dmlPublic = getDomainMultiDml(domain, tableName, secondaryTable, false, variant);
        String dmlHidden = getDomainMultiDml(domain, tableName, secondaryTable, true, variant);

        String refQuery = getMediumQuery(domain, tableName, secondaryTable, patternIdx, variant);
        String desc = buildDescription(title, "MEDIUM", domain, ddl, dmlPublic, refQuery);

        List<GeneratedTestCase> testCases = List.of(
                new GeneratedTestCase(1, false, ddl + "\n" + dmlPublic, refQuery),
                new GeneratedTestCase(2, true, ddl + "\n" + dmlHidden, refQuery)
        );

        List<String> tags = List.of("Database", "SQL", "MySQL", capitalize(domain), getMediumTagName(patternIdx));
        List<String> constraints = List.of(
                "Table contains between 1 and 2000 rows.",
                "No circular relationships exist in self-referential keys."
        );

        return new GeneratedProblem(
                sourceId,
                slug,
                title,
                "MEDIUM",
                "DATABASE",
                tags,
                constraints,
                desc,
                ddl + "\n" + dmlPublic,
                "Result set ordered according to problem specification.",
                STARTER_CODE,
                LANG_CONFIG,
                1,
                testCases
        );
    }

    private static GeneratedProblem generateHardProblem(int id) {
        String sourceId = String.format("sql-%04d", id);
        int offset = id - 961;
        int domainIdx = offset % DOMAINS.length;
        String domain = DOMAINS[domainIdx];
        int patternIdx = (offset / DOMAINS.length) % 16;
        int variant = (offset / (DOMAINS.length * 16)) + 1;

        String title = buildTitle("HARD", domain, patternIdx, variant, id);
        String slug = buildSlug(sourceId, title);
        String tableName = getDomainTable(domain);
        String secondaryTable = getDomainSecondaryTable(domain);

        String ddl = getDomainMultiDdl(domain, tableName, secondaryTable);
        String dmlPublic = getDomainMultiDml(domain, tableName, secondaryTable, false, variant);
        String dmlHidden = getDomainMultiDml(domain, tableName, secondaryTable, true, variant);

        String refQuery = getHardQuery(domain, tableName, secondaryTable, patternIdx, variant);
        String desc = buildDescription(title, "HARD", domain, ddl, dmlPublic, refQuery);

        List<GeneratedTestCase> testCases = List.of(
                new GeneratedTestCase(1, false, ddl + "\n" + dmlPublic, refQuery),
                new GeneratedTestCase(2, true, ddl + "\n" + dmlHidden, refQuery)
        );

        List<String> tags = List.of("Database", "SQL", "MySQL", capitalize(domain), getHardTagName(patternIdx));
        List<String> constraints = List.of(
                "Input tables contain between 1 and 5000 rows.",
                "Temporal timestamps are guaranteed to be in UTC format."
        );

        return new GeneratedProblem(
                sourceId,
                slug,
                title,
                "HARD",
                "DATABASE",
                tags,
                constraints,
                desc,
                ddl + "\n" + dmlPublic,
                "Result set ordered according to problem specification.",
                STARTER_CODE,
                LANG_CONFIG,
                2,
                testCases
        );
    }

    // ==========================================
    // DOMAIN CONFIGURATIONS & SCHEMAS
    // ==========================================

    private static String getDomainTable(String domain) {
        return switch (domain) {
            case "hr" -> "Employees";
            case "ecommerce" -> "Orders";
            case "rideshare" -> "Trips";
            case "social" -> "Posts";
            case "banking" -> "Transactions";
            case "healthcare" -> "Appointments";
            case "education" -> "Enrollments";
            case "streaming" -> "Streams";
            case "gaming" -> "Matches";
            case "realestate" -> "Bookings";
            case "saas" -> "ApiRequests";
            case "supplychain" -> "Shipments";
            case "helpdesk" -> "Tickets";
            case "marketing" -> "AdClicks";
            case "telemetry" -> "SensorLogs";
            default -> "DataRecords";
        };
    }

    private static String getDomainSecondaryTable(String domain) {
        return switch (domain) {
            case "hr" -> "Departments";
            case "ecommerce" -> "Customers";
            case "rideshare" -> "Users";
            case "social" -> "Accounts";
            case "banking" -> "BankAccounts";
            case "healthcare" -> "Patients";
            case "education" -> "Courses";
            case "streaming" -> "Tracks";
            case "gaming" -> "Players";
            case "realestate" -> "Properties";
            case "saas" -> "Tenants";
            case "supplychain" -> "Warehouses";
            case "helpdesk" -> "SupportAgents";
            case "marketing" -> "Campaigns";
            case "telemetry" -> "Devices";
            default -> "ReferenceData";
        };
    }

    private static String getDomainDdl(String domain, String table) {
        return switch (domain) {
            case "hr" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, name VARCHAR(50), salary INT, department_id INT, manager_id INT, hire_date DATE);";
            case "ecommerce" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, customer_id INT, order_date DATE, amount INT, status VARCHAR(20));";
            case "rideshare" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, client_id INT, driver_id INT, city_id INT, fare INT, status VARCHAR(20), request_at DATE);";
            case "social" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, user_id INT, post_date DATE, likes INT, views INT);";
            case "banking" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, account_id INT, txn_date DATE, amount INT, txn_type VARCHAR(20));";
            case "healthcare" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, patient_id INT, doctor_id INT, visit_date DATE, fee INT);";
            case "education" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, student_id INT, course_id INT, grade INT, enroll_date DATE);";
            case "streaming" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, user_id INT, track_id INT, duration INT, stream_date DATE);";
            case "gaming" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, player_id INT, score INT, duration INT, match_date DATE);";
            case "realestate" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, property_id INT, guest_id INT, price INT, booking_date DATE);";
            case "saas" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, tenant_id INT, endpoint VARCHAR(50), response_time INT, req_date DATE);";
            case "supplychain" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, warehouse_id INT, product_id INT, quantity INT, ship_date DATE);";
            case "helpdesk" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, agent_id INT, priority VARCHAR(20), resolve_time INT, ticket_date DATE);";
            case "marketing" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, campaign_id INT, clicks INT, conversions INT, click_date DATE);";
            case "telemetry" -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, device_id INT, temperature INT, battery INT, log_date DATE);";
            default -> "CREATE TABLE IF NOT EXISTS " + table + " (id INT PRIMARY KEY, val INT, record_date DATE);";
        };
    }

    private static String getDomainMultiDdl(String domain, String table, String secTable) {
        String mainDdl = getDomainDdl(domain, table);
        String secDdl = switch (domain) {
            case "hr" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), budget INT);";
            case "ecommerce" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), country VARCHAR(50));";
            case "rideshare" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), banned VARCHAR(10), role VARCHAR(20));";
            case "social" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), verified INT);";
            case "banking" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), balance INT);";
            case "healthcare" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), age INT);";
            case "education" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), credits INT);";
            case "streaming" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), genre VARCHAR(30));";
            case "gaming" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), rank_tier VARCHAR(20));";
            case "realestate" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), max_guests INT);";
            case "saas" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), plan_tier VARCHAR(20));";
            case "supplychain" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), capacity INT);";
            case "helpdesk" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), department VARCHAR(30));";
            case "marketing" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), channel VARCHAR(30));";
            case "telemetry" -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50), firm_ver VARCHAR(20));";
            default -> "CREATE TABLE IF NOT EXISTS " + secTable + " (id INT PRIMARY KEY, name VARCHAR(50));";
        };
        return mainDdl + "\n" + secDdl;
    }

    private static String getDomainDml(String domain, String table, boolean hidden, int variant) {
        int delta = hidden ? 50 : 0;
        int shift = (variant - 1) * 10;
        return switch (domain) {
            case "hr" -> String.format(
                    "INSERT INTO %s VALUES (1, 'Alice', %d, 1, NULL, '2022-01-15'), (2, 'Bob', %d, 1, 1, '2022-03-20'), (3, 'Charlie', %d, 2, NULL, '2021-06-10'), (4, 'David', %d, 2, 3, '2023-01-05'), (5, 'Eve', %d, 1, 1, '2023-02-14');",
                    table, 70000 + delta + shift, 80000 + delta + shift, 90000 + delta + shift, 65000 + delta + shift, 85000 + delta + shift);
            case "ecommerce" -> String.format(
                    "INSERT INTO %s VALUES (1, 101, '2023-01-10', %d, 'completed'), (2, 102, '2023-01-15', %d, 'cancelled'), (3, 101, '2023-02-01', %d, 'completed'), (4, 103, '2023-02-14', %d, 'completed'), (5, 102, '2023-03-01', %d, 'completed');",
                    table, 150 + delta, 200 + delta, 300 + delta, 80 + delta, 450 + delta);
            case "rideshare" -> String.format(
                    "INSERT INTO %s VALUES (1, 1, 10, 1, %d, 'completed', '2023-10-01'), (2, 2, 11, 1, %d, 'cancelled_by_driver', '2023-10-01'), (3, 3, 12, 6, %d, 'completed', '2023-10-02'), (4, 4, 13, 6, %d, 'cancelled_by_client', '2023-10-02'), (5, 1, 10, 1, %d, 'completed', '2023-10-03');",
                    table, 25 + delta, 15 + delta, 40 + delta, 30 + delta, 20 + delta);
            case "social" -> String.format(
                    "INSERT INTO %s VALUES (1, 201, '2023-05-01', %d, %d), (2, 202, '2023-05-02', %d, %d), (3, 201, '2023-05-03', %d, %d), (4, 203, '2023-05-04', %d, %d), (5, 202, '2023-05-05', %d, %d);",
                    table, 120 + delta, 1500 + delta * 10, 45 + delta, 600 + delta * 10, 300 + delta, 4000 + delta * 10, 10 + delta, 150 + delta * 10, 500 + delta, 6200 + delta * 10);
            case "banking" -> String.format(
                    "INSERT INTO %s VALUES (1, 501, '2023-04-01', %d, 'deposit'), (2, 501, '2023-04-05', %d, 'withdrawal'), (3, 502, '2023-04-07', %d, 'deposit'), (4, 503, '2023-04-10', %d, 'deposit'), (5, 502, '2023-04-12', %d, 'withdrawal');",
                    table, 1000 + delta, 200 + delta, 1500 + delta, 3000 + delta, 500 + delta);
            case "healthcare" -> String.format(
                    "INSERT INTO %s VALUES (1, 101, 1, '2023-01-10', %d), (2, 102, 2, '2023-01-15', %d), (3, 101, 1, '2023-02-01', %d), (4, 103, 3, '2023-02-14', %d), (5, 102, 2, '2023-03-01', %d);",
                    table, 150 + delta, 200 + delta, 180 + delta, 250 + delta, 300 + delta);
            case "education" -> String.format(
                    "INSERT INTO %s VALUES (1, 201, 1, %d, '2023-09-01'), (2, 202, 1, %d, '2023-09-02'), (3, 201, 2, %d, '2023-09-03'), (4, 203, 3, %d, '2023-09-04'), (5, 202, 2, %d, '2023-09-05');",
                    table, 85 + delta, 92 + delta, 78 + delta, 88 + delta, 95 + delta);
            case "streaming" -> String.format(
                    "INSERT INTO %s VALUES (1, 301, 1, %d, '2023-06-01'), (2, 302, 2, %d, '2023-06-02'), (3, 301, 3, %d, '2023-06-03'), (4, 303, 1, %d, '2023-06-04'), (5, 302, 4, %d, '2023-06-05');",
                    table, 210 + delta, 185 + delta, 240 + delta, 210 + delta, 300 + delta);
            case "gaming" -> String.format(
                    "INSERT INTO %s VALUES (1, 401, %d, 15, '2023-08-01'), (2, 402, %d, 25, '2023-08-02'), (3, 401, %d, 20, '2023-08-03'), (4, 403, %d, 12, '2023-08-04'), (5, 402, %d, 30, '2023-08-05');",
                    table, 1250 + delta * 10, 2100 + delta * 10, 1800 + delta * 10, 950 + delta * 10, 2400 + delta * 10);
            case "realestate" -> String.format(
                    "INSERT INTO %s VALUES (1, 501, 101, %d, '2023-07-01'), (2, 502, 102, %d, '2023-07-05'), (3, 501, 103, %d, '2023-07-10'), (4, 503, 101, %d, '2023-07-15'), (5, 502, 104, %d, '2023-07-20');",
                    table, 150 + delta, 220 + delta, 160 + delta, 310 + delta, 200 + delta);
            case "saas" -> String.format(
                    "INSERT INTO %s VALUES (1, 601, '/api/v1/auth', %d, '2023-05-10'), (2, 602, '/api/v1/users', %d, '2023-05-11'), (3, 601, '/api/v1/reports', %d, '2023-05-12'), (4, 603, '/api/v1/auth', %d, '2023-05-13'), (5, 602, '/api/v1/billing', %d, '2023-05-14');",
                    table, 45 + delta, 120 + delta, 350 + delta, 50 + delta, 210 + delta);
            case "supplychain" -> String.format(
                    "INSERT INTO %s VALUES (1, 701, 1, %d, '2023-03-01'), (2, 702, 2, %d, '2023-03-05'), (3, 701, 3, %d, '2023-03-10'), (4, 703, 1, %d, '2023-03-15'), (5, 702, 4, %d, '2023-03-20');",
                    table, 500 + delta, 1200 + delta, 350 + delta, 800 + delta, 150 + delta);
            case "helpdesk" -> String.format(
                    "INSERT INTO %s VALUES (1, 801, 'High', %d, '2023-04-01'), (2, 802, 'Low', %d, '2023-04-02'), (3, 801, 'Critical', %d, '2023-04-03'), (4, 803, 'Medium', %d, '2023-04-04'), (5, 802, 'High', %d, '2023-04-05');",
                    table, 35 + delta, 15 + delta, 90 + delta, 25 + delta, 45 + delta);
            case "marketing" -> String.format(
                    "INSERT INTO %s VALUES (1, 901, 450, %d, '2023-02-01'), (2, 902, 1200, %d, '2023-02-05'), (3, 901, 600, %d, '2023-02-10'), (4, 903, 300, %d, '2023-02-15'), (5, 902, 1500, %d, '2023-02-20');",
                    table, 45 + delta, 150 + delta, 70 + delta, 20 + delta, 180 + delta);
            case "telemetry" -> String.format(
                    "INSERT INTO %s VALUES (1, 1001, %d, 95, '2023-01-01'), (2, 1002, %d, 88, '2023-01-02'), (3, 1001, %d, 92, '2023-01-03'), (4, 1003, %d, 75, '2023-01-04'), (5, 1002, %d, 84, '2023-01-05');",
                    table, 24 + delta, 28 + delta, 26 + delta, 31 + delta, 29 + delta);
            default -> String.format(
                    "INSERT INTO %s VALUES (1, 1, '2023-01-01', %d, %d), (2, 1, '2023-01-02', %d, %d), (3, 2, '2023-01-03', %d, %d), (4, 2, '2023-01-04', %d, %d), (5, 3, '2023-01-05', %d, %d);",
                    table, 50 + delta, 100 + delta, 75 + delta, 150 + delta, 90 + delta, 180 + delta, 40 + delta, 80 + delta, 110 + delta, 220 + delta);
        };
    }

    private static String getDomainMultiDml(String domain, String table, String secTable, boolean hidden, int variant) {
        String mainDml = getDomainDml(domain, table, hidden, variant);
        String secDml = switch (domain) {
            case "hr" -> String.format("INSERT INTO %s VALUES (1, 'Engineering', 500000), (2, 'Sales', 300000), (3, 'HR', 150000);", secTable);
            case "ecommerce" -> String.format("INSERT INTO %s VALUES (101, 'John Doe', 'USA'), (102, 'Jane Smith', 'Canada'), (103, 'Carlos Ray', 'Mexico'), (104, 'No Orders', 'UK');", secTable);
            case "rideshare" -> String.format("INSERT INTO %s VALUES (1, 'Client 1', 'No', 'client'), (2, 'Client 2', 'Yes', 'client'), (3, 'Client 3', 'No', 'client'), (4, 'Client 4', 'No', 'client'), (10, 'Driver 10', 'No', 'driver'), (11, 'Driver 11', 'No', 'driver'), (12, 'Driver 12', 'No', 'driver'), (13, 'Driver 13', 'No', 'driver');", secTable);
            case "social" -> String.format("INSERT INTO %s VALUES (201, 'tech_guru', 1), (202, 'traveler', 0), (203, 'foodie', 1), (204, 'inactive_user', 0);", secTable);
            case "banking" -> String.format("INSERT INTO %s VALUES (501, 'Downtown', 12000), (502, 'Uptown', 25000), (503, 'Westside', 8000), (504, 'Suburbs', 0);", secTable);
            case "healthcare" -> String.format("INSERT INTO %s VALUES (1, 'Patient Alpha', 34), (2, 'Patient Beta', 45), (3, 'Patient Gamma', 62), (4, 'Patient Delta', 19);", secTable);
            case "education" -> String.format("INSERT INTO %s VALUES (1, 'Database Systems', 4), (2, 'Algorithms', 4), (3, 'Operating Systems', 3), (4, 'Web Development', 3);", secTable);
            case "streaming" -> String.format("INSERT INTO %s VALUES (1, 'Bohemian Rhapsody', 'Rock'), (2, 'Blinding Lights', 'Pop'), (3, 'Shape of You', 'Pop'), (4, 'Hotel California', 'Rock');", secTable);
            case "gaming" -> String.format("INSERT INTO %s VALUES (1, 'AceShooter', 'Diamond'), (2, 'ShadowNinja', 'Platinum'), (3, 'PixelHero', 'Gold'), (4, 'Rookie', 'Bronze');", secTable);
            case "realestate" -> String.format("INSERT INTO %s VALUES (1, 'New York', 4), (2, 'San Francisco', 2), (3, 'Austin', 6), (4, 'Miami', 8);", secTable);
            case "saas" -> String.format("INSERT INTO %s VALUES (1, 'Acme Corp', 'Enterprise'), (2, 'Beta LLC', 'Pro'), (3, 'Gamma Inc', 'Starter'), (4, 'Delta Co', 'Enterprise');", secTable);
            case "supplychain" -> String.format("INSERT INTO %s VALUES (1, 'Central Hub', 10000), (2, 'East Coast Depot', 5000), (3, 'West Port', 8000);", secTable);
            case "helpdesk" -> String.format("INSERT INTO %s VALUES (1, 'Support Sarah', 'Tier 2'), (2, 'Helpdesk Dan', 'Tier 1'), (3, 'Tech Mike', 'Tier 3');", secTable);
            case "marketing" -> String.format("INSERT INTO %s VALUES (1, 'Summer Launch', 'Social'), (2, 'Black Friday', 'Search'), (3, 'Brand Re-engage', 'Email');", secTable);
            case "telemetry" -> String.format("INSERT INTO %s VALUES (1, 'SensorPro-V2', '1.4.2'), (2, 'EcoSensor-X', '2.1.0'), (3, 'Industrial-Z', '0.9.8');", secTable);
            default -> String.format("INSERT INTO %s VALUES (1, 'Standard Unit'), (2, 'Advanced Unit');", secTable);
        };
        return mainDml + "\n" + secDml;
    }

    // ==========================================
    // QUERY BUILDERS
    // ==========================================

    private static String getEasyQuery(String domain, String table, int pattern, int variant) {
        return switch (pattern) {
            case 0 -> String.format("SELECT * FROM %s WHERE id <= 3 ORDER BY id ASC;", table);
            case 1 -> String.format("SELECT id, %s FROM %s ORDER BY id DESC LIMIT 2;", getColForDomain(domain, 1), table);
            case 2 -> String.format("SELECT COUNT(*) AS total_records FROM %s;", table);
            case 3 -> String.format("SELECT %s, COUNT(*) AS cnt FROM %s GROUP BY %s ORDER BY %s ASC;", getColForDomain(domain, 2), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 4 -> String.format("SELECT %s, COUNT(*) AS cnt FROM %s GROUP BY %s HAVING COUNT(*) >= 1 ORDER BY cnt DESC, %s ASC;", getColForDomain(domain, 2), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 5 -> String.format("SELECT DISTINCT %s FROM %s ORDER BY %s ASC;", getColForDomain(domain, 2), table, getColForDomain(domain, 2));
            case 6 -> String.format("SELECT id, %s FROM %s WHERE %s IS NOT NULL ORDER BY id ASC;", getColForDomain(domain, 1), table, getColForDomain(domain, 1));
            case 7 -> String.format("SELECT id, %s * 2 AS doubled_metric FROM %s ORDER BY id ASC;", getMetricColForDomain(domain), table);
            case 8 -> String.format("SELECT id, CASE WHEN %s > 100 THEN 'High' ELSE 'Low' END AS category_tier FROM %s ORDER BY id ASC;", getMetricColForDomain(domain), table);
            case 9 -> String.format("SELECT id, %s FROM %s WHERE id IN (1, 3, 5) ORDER BY id ASC;", getColForDomain(domain, 1), table);
            case 10 -> String.format("SELECT id, %s FROM %s WHERE id BETWEEN 2 AND 4 ORDER BY id ASC;", getColForDomain(domain, 1), table);
            default -> String.format("SELECT MIN(%s) AS min_val, MAX(%s) AS max_val FROM %s;", getMetricColForDomain(domain), getMetricColForDomain(domain), table);
        };
    }

    private static String getMediumQuery(String domain, String table, String secTable, int pattern, int variant) {
        return switch (pattern) {
            case 0 -> String.format(
                    "SELECT t.id, s.name AS entity_name FROM %s t INNER JOIN %s s ON t.%s = s.id ORDER BY t.id ASC;",
                    table, secTable, getForeignKeyCol(domain));
            case 1 -> String.format(
                    "SELECT s.id, s.name FROM %s s LEFT JOIN %s t ON s.id = t.%s WHERE t.id IS NULL ORDER BY s.id ASC;",
                    secTable, table, getForeignKeyCol(domain));
            case 2 -> String.format(
                    "SELECT t.id, t.%s, (SELECT MAX(%s) FROM %s) AS max_overall FROM %s t ORDER BY t.id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table, table);
            case 3 -> String.format(
                    "SELECT t.%s, AVG(t.%s) AS avg_metric FROM %s t GROUP BY t.%s HAVING AVG(t.%s) > 0 ORDER BY t.%s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getMetricColForDomain(domain), getColForDomain(domain, 2));
            case 4 -> String.format(
                    "SELECT id, %s, DENSE_RANK() OVER (ORDER BY %s DESC) AS ranking FROM %s ORDER BY ranking ASC, id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 5 -> String.format(
                    "SELECT id, %s, ROW_NUMBER() OVER (PARTITION BY %s ORDER BY id ASC) AS row_num FROM %s ORDER BY id ASC;",
                    getColForDomain(domain, 2), getColForDomain(domain, 2), table);
            case 6 -> String.format(
                    "SELECT id, %s, LAG(%s, 1) OVER (ORDER BY id ASC) AS prev_val FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 7 -> String.format(
                    "SELECT id, %s, LEAD(%s, 1) OVER (ORDER BY id ASC) AS next_val FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 8 -> String.format(
                    "SELECT id, %s, SUM(%s) OVER (ORDER BY id ASC) AS running_total FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 9 -> String.format(
                    "WITH SummaryCTE AS (SELECT %s, COUNT(*) AS item_count FROM %s GROUP BY %s) SELECT * FROM SummaryCTE WHERE item_count >= 1 ORDER BY %s ASC;",
                    getColForDomain(domain, 2), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 10 -> String.format(
                    "SELECT id, %s, 'BatchA' AS batch_group FROM %s WHERE id <= 2 UNION ALL SELECT id, %s, 'BatchB' AS batch_group FROM %s WHERE id > 2 ORDER BY id ASC;",
                    getColForDomain(domain, 1), table, getColForDomain(domain, 1), table);
            case 11 -> String.format(
                    "SELECT %s, SUM(CASE WHEN %s > 50 THEN 1 ELSE 0 END) AS high_count FROM %s GROUP BY %s ORDER BY %s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 12 -> String.format(
                    "SELECT id, %s, ROUND(%s * 1.15, 2) AS adjusted_metric FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 13 -> String.format(
                    "SELECT a.id, a.%s AS current_val, b.%s AS match_val FROM %s a INNER JOIN %s b ON a.id = b.id - 1 ORDER BY a.id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table, table);
            case 14 -> String.format(
                    "SELECT %s, COUNT(DISTINCT id) AS unique_entries FROM %s GROUP BY %s ORDER BY unique_entries DESC, %s ASC;",
                    getColForDomain(domain, 2), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 15 -> String.format(
                    "SELECT s.name, COALESCE(SUM(t.%s), 0) AS total_metric FROM %s s LEFT JOIN %s t ON s.id = t.%s GROUP BY s.id, s.name ORDER BY s.name ASC;",
                    getMetricColForDomain(domain), secTable, table, getForeignKeyCol(domain));
            case 16 -> String.format(
                    "SELECT id, %s FROM %s WHERE %s > (SELECT AVG(%s) FROM %s) ORDER BY id ASC;",
                    getMetricColForDomain(domain), table, getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 17 -> String.format(
                    "SELECT DISTINCT a.id FROM %s a WHERE EXISTS (SELECT 1 FROM %s b WHERE b.id = a.id AND b.%s IS NOT NULL) ORDER BY a.id ASC;",
                    table, table, getMetricColForDomain(domain));
            case 18 -> String.format(
                    "SELECT %s, MIN(%s) AS min_metric, MAX(%s) AS max_metric FROM %s GROUP BY %s ORDER BY %s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            default -> String.format(
                    "SELECT id, %s, NTILE(2) OVER (ORDER BY %s ASC) AS quartile_bucket FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table);
        };
    }

    private static String getHardQuery(String domain, String table, String secTable, int pattern, int variant) {
        return switch (pattern) {
            case 0 -> String.format(
                    "WITH Ranked AS (SELECT t.*, DENSE_RANK() OVER (PARTITION BY t.%s ORDER BY t.%s DESC) AS rnk FROM %s t) SELECT id, %s, %s, rnk FROM Ranked WHERE rnk <= 2 ORDER BY %s ASC, rnk ASC, id ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getMetricColForDomain(domain), getColForDomain(domain, 2));
            case 1 -> String.format(
                    "SELECT DISTINCT a.%s AS consecutive_val FROM %s a JOIN %s b ON a.id = b.id - 1 AND a.%s = b.%s JOIN %s c ON a.id = c.id - 2 AND a.%s = c.%s ORDER BY consecutive_val ASC;",
                    getColForDomain(domain, 2), table, table, getColForDomain(domain, 2), getColForDomain(domain, 2), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 2 -> String.format(
                    "WITH ActiveMetrics AS (SELECT t.id, t.%s, t.%s FROM %s t WHERE t.%s > 0) SELECT a.%s, COUNT(*) AS active_count, ROUND(AVG(a.%s), 2) AS avg_active FROM ActiveMetrics a GROUP BY a.%s HAVING active_count >= 1 ORDER BY a.%s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getMetricColForDomain(domain), getColForDomain(domain, 2), getMetricColForDomain(domain), getColForDomain(domain, 2), getColForDomain(domain, 2));
            case 3 -> String.format(
                    "WITH IslandCTE AS (SELECT id, %s, id - ROW_NUMBER() OVER (ORDER BY id ASC) AS grp FROM %s) SELECT grp, MIN(id) AS start_id, MAX(id) AS end_id, COUNT(*) AS streak_len FROM IslandCTE GROUP BY grp ORDER BY start_id ASC;",
                    getColForDomain(domain, 2), table);
            case 4 -> String.format(
                    "WITH RECURSIVE HierarchyCTE AS (SELECT id, 1 AS depth FROM %s WHERE id = 1 UNION ALL SELECT t.id, h.depth + 1 FROM %s t INNER JOIN HierarchyCTE h ON t.id = h.id + 1 WHERE h.depth < 3) SELECT id, depth FROM HierarchyCTE ORDER BY depth ASC, id ASC;",
                    table, table);
            case 5 -> String.format(
                    "SELECT s.name AS sec_name, COUNT(t.id) AS total_items, COALESCE(SUM(t.%s), 0) AS aggregate_val, ROUND(AVG(COALESCE(t.%s, 0)), 2) AS mean_val FROM %s s LEFT JOIN %s t ON s.id = t.%s GROUP BY s.id, s.name ORDER BY aggregate_val DESC, s.name ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), secTable, table, getForeignKeyCol(domain));
            case 6 -> String.format(
                    "WITH CumulativeCTE AS (SELECT id, %s, %s, SUM(%s) OVER (PARTITION BY %s ORDER BY id ASC) AS cum_sum FROM %s) SELECT * FROM CumulativeCTE WHERE cum_sum > 0 ORDER BY %s ASC, id ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), getMetricColForDomain(domain), getColForDomain(domain, 2), table, getColForDomain(domain, 2));
            case 7 -> String.format(
                    "SELECT id, %s, FIRST_VALUE(%s) OVER (ORDER BY id ASC) AS first_seen, LAST_VALUE(%s) OVER (ORDER BY id ASC RANGE BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) AS last_seen FROM %s ORDER BY id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 8 -> String.format(
                    "SELECT a.id, a.%s, (SELECT COUNT(DISTINCT b.%s) FROM %s b WHERE b.%s >= a.%s) AS rank_pos FROM %s a ORDER BY rank_pos ASC, a.id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), table, getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 9 -> String.format(
                    "WITH PctCTE AS (SELECT id, %s, %s, PERCENT_RANK() OVER (ORDER BY %s ASC) AS pct_rank FROM %s) SELECT id, %s, ROUND(pct_rank, 4) AS percentile FROM PctCTE ORDER BY id ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), getMetricColForDomain(domain), table, getMetricColForDomain(domain));
            case 10 -> String.format(
                    "WITH BaseCTE AS (SELECT %s, SUM(%s) AS cat_sum FROM %s GROUP BY %s) SELECT b.%s, b.cat_sum, ROUND(100.0 * b.cat_sum / (SELECT SUM(%s) FROM %s), 2) AS pct_of_total FROM BaseCTE b ORDER BY pct_of_total DESC, b.%s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2));
            case 11 -> String.format(
                    "SELECT t.id, t.%s, s.name, CASE WHEN t.%s >= 1000 THEN 'Platinum' WHEN t.%s >= 500 THEN 'Gold' ELSE 'Standard' END AS tier_grade FROM %s t INNER JOIN %s s ON t.%s = s.id ORDER BY t.id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), getMetricColForDomain(domain), table, secTable, getForeignKeyCol(domain));
            case 12 -> String.format(
                    "WITH MultiRank AS (SELECT id, %s, %s, ROW_NUMBER() OVER (ORDER BY id ASC) AS rn, DENSE_RANK() OVER (ORDER BY %s DESC) AS dr FROM %s) SELECT id, %s, rn, dr FROM MultiRank WHERE rn <= 4 ORDER BY dr ASC, id ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), getMetricColForDomain(domain), table, getMetricColForDomain(domain));
            case 13 -> String.format(
                    "SELECT a.id, a.%s, a.%s - COALESCE(LAG(a.%s) OVER (ORDER BY a.id ASC), a.%s) AS delta_diff FROM %s a ORDER BY a.id ASC;",
                    getMetricColForDomain(domain), getMetricColForDomain(domain), getMetricColForDomain(domain), getMetricColForDomain(domain), table);
            case 14 -> String.format(
                    "WITH GroupedCTE AS (SELECT %s, COUNT(*) AS record_count, AVG(%s) AS avg_metric FROM %s GROUP BY %s) SELECT g.*, DENSE_RANK() OVER (ORDER BY g.avg_metric DESC) AS quality_rank FROM GroupedCTE g ORDER BY quality_rank ASC, g.%s ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), table, getColForDomain(domain, 2), getColForDomain(domain, 2));
            default -> String.format(
                    "WITH FinalHardCTE AS (SELECT id, %s, %s, NTILE(4) OVER (ORDER BY %s ASC) AS quartile FROM %s) SELECT id, %s, quartile FROM FinalHardCTE WHERE quartile IN (1, 4) ORDER BY quartile ASC, id ASC;",
                    getColForDomain(domain, 2), getMetricColForDomain(domain), getMetricColForDomain(domain), table, getMetricColForDomain(domain));
        };
    }

    private static String getColForDomain(String domain, int colIndex) {
        return switch (domain) {
            case "hr" -> colIndex == 1 ? "name" : "department_id";
            case "ecommerce" -> colIndex == 1 ? "status" : "customer_id";
            case "rideshare" -> colIndex == 1 ? "status" : "city_id";
            case "social" -> colIndex == 1 ? "post_date" : "user_id";
            case "banking" -> colIndex == 1 ? "txn_type" : "account_id";
            case "healthcare" -> colIndex == 1 ? "visit_date" : "doctor_id";
            case "education" -> colIndex == 1 ? "enroll_date" : "course_id";
            case "streaming" -> colIndex == 1 ? "stream_date" : "track_id";
            case "gaming" -> colIndex == 1 ? "match_date" : "player_id";
            case "realestate" -> colIndex == 1 ? "booking_date" : "property_id";
            case "saas" -> colIndex == 1 ? "endpoint" : "tenant_id";
            case "supplychain" -> colIndex == 1 ? "ship_date" : "warehouse_id";
            case "helpdesk" -> colIndex == 1 ? "priority" : "agent_id";
            case "marketing" -> colIndex == 1 ? "click_date" : "campaign_id";
            case "telemetry" -> colIndex == 1 ? "log_date" : "device_id";
            default -> colIndex == 1 ? "val" : "id";
        };
    }

    private static String getMetricColForDomain(String domain) {
        return switch (domain) {
            case "hr" -> "salary";
            case "ecommerce" -> "amount";
            case "rideshare" -> "fare";
            case "social" -> "likes";
            case "banking" -> "amount";
            case "healthcare" -> "fee";
            case "education" -> "grade";
            case "streaming" -> "duration";
            case "gaming" -> "score";
            case "realestate" -> "price";
            case "saas" -> "response_time";
            case "supplychain" -> "quantity";
            case "helpdesk" -> "resolve_time";
            case "marketing" -> "conversions";
            case "telemetry" -> "temperature";
            default -> "val";
        };
    }

    private static String getForeignKeyCol(String domain) {
        return switch (domain) {
            case "hr" -> "department_id";
            case "ecommerce" -> "customer_id";
            case "rideshare" -> "client_id";
            case "social" -> "user_id";
            case "banking" -> "account_id";
            case "healthcare" -> "patient_id";
            case "education" -> "course_id";
            case "streaming" -> "track_id";
            case "gaming" -> "player_id";
            case "realestate" -> "property_id";
            case "saas" -> "tenant_id";
            case "supplychain" -> "warehouse_id";
            case "helpdesk" -> "agent_id";
            case "marketing" -> "campaign_id";
            case "telemetry" -> "device_id";
            default -> "id";
        };
    }

    private static String buildTitle(String difficulty, String domain, int pattern, int variant, int id) {
        String domainCap = capitalize(domain);
        String patternName = switch (difficulty) {
            case "EASY" -> switch (pattern) {
                case 0 -> "Basic Record Lookup";
                case 1 -> "Top Metrics Selection";
                case 2 -> "Total Entries Counter";
                case 3 -> "Category Frequency Breakdown";
                case 4 -> "Threshold Group Filter";
                case 5 -> "Distinct Value Extraction";
                case 6 -> "Non-Null Record Query";
                case 7 -> "Metric Scale Transformation";
                case 8 -> "Conditional Tier Categorization";
                case 9 -> "Specific ID Filtering";
                case 10 -> "Range Interval Extraction";
                default -> "Min-Max Metric Extrema";
            };
            case "MEDIUM" -> switch (pattern) {
                case 0 -> "Inner Relation Pairing";
                case 1 -> "Orphaned Entity Detection";
                case 2 -> "Correlated Maximum Benchmark";
                case 3 -> "Conditional Group Mean";
                case 4 -> "Dense Rank Evaluation";
                case 5 -> "Sequential Row Indexing";
                case 6 -> "Lagged Historical Lookback";
                case 7 -> "Forward Lead Projection";
                case 8 -> "Cumulative Running Balance";
                case 9 -> "CTE Aggregation Pipeline";
                case 10 -> "Disjoint Set Union";
                case 11 -> "Conditional Counter Aggregation";
                case 12 -> "Multiplier Adjustment Calculation";
                case 13 -> "Adjacent Pair Self-Join";
                case 14 -> "Cardinality Grouping";
                case 15 -> "Parent-Child Aggregator";
                case 16 -> "Super-Average Outlier Filter";
                case 17 -> "Existence Subquery Matcher";
                case 18 -> "Spread Extrema Comparison";
                default -> "Quartile Bucket Partitioning";
            };
            default -> switch (pattern) {
                case 0 -> "Top Per Category Dense Ranking";
                case 1 -> "Triple Consecutive Sequence Matcher";
                case 2 -> "Multi-Condition CTE Filtering";
                case 3 -> "Gaps and Islands Contiguous Detection";
                case 4 -> "Recursive Hierarchy Path Traversal";
                case 5 -> "Comprehensive Multi-Join Ledger";
                case 6 -> "Partitioned Cumulative Windowing";
                case 7 -> "Boundary Frame First-Last Extraction";
                case 8 -> "Correlated Distinct Competitive Rank";
                case 9 -> "Percentile Distribution Analytics";
                case 10 -> "Relative Share Proportion CTE";
                case 11 -> "Tiered Classification Hybrid Query";
                case 12 -> "Dual Window Order Correlation";
                case 13 -> "Delta Change Rate Differential";
                case 14 -> "Ranked Category Performance Index";
                default -> "Extreme Quartile Tail Analysis";
            };
        };
        return String.format("%s %s: %s (Variant %d)", domainCap, difficulty, patternName, variant);
    }

    private static String buildSlug(String sourceId, String title) {
        String clean = title.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("^-+|-+$", "");
        return sourceId + "-" + clean;
    }

    private static String buildDescription(String title, String difficulty, String domain, String ddl, String dml, String refQuery) {
        return String.format("""
                # %s
                
                **Difficulty**: `%s`  
                **Category**: `Database / SQL`  
                **Domain**: `%s`
                
                ## Problem Statement
                You are given the relational database schema outlined below. Write a clean, high-performance MySQL query to produce the expected result set according to the specifications.
                
                ### Table Schema
                ```sql
                %s
                ```
                
                ### Sample Data
                ```sql
                %s
                ```
                
                ### Expected Output
                The query should evaluate against the schema and return the exact columns and rows required.
                Order your output as requested in the problem or use standard ordering clauses where appropriate.
                
                ### Reference Approach
                ```sql
                %s
                ```
                """, title, difficulty, capitalize(domain), ddl, dml, refQuery);
    }

    private static String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return Character.toUpperCase(str.charAt(0)) + str.substring(1);
    }

    private static String getEasyTagName(int pattern) {
        return switch (pattern) {
            case 0, 1, 9, 10 -> "Basic Filtering";
            case 2, 3, 4, 11 -> "Aggregations";
            case 5, 6 -> "Distinct Selection";
            case 7, 8 -> "Conditional Expressions";
            default -> "SQL Fundamentals";
        };
    }

    private static String getMediumTagName(int pattern) {
        return switch (pattern) {
            case 0, 1, 13, 15 -> "Joins";
            case 2, 16, 17 -> "Subqueries";
            case 4, 5, 6, 7, 8, 19 -> "Window Functions";
            case 9 -> "Common Table Expressions";
            case 10 -> "Set Operations";
            default -> "Analytical SQL";
        };
    }

    private static String getHardTagName(int pattern) {
        return switch (pattern) {
            case 0, 6, 7, 9, 12, 13 -> "Advanced Window Functions";
            case 1, 3 -> "Consecutive Records & Gaps";
            case 4 -> "Recursive CTEs";
            case 2, 10, 14, 15 -> "Complex Analytics";
            default -> "Database Mastery";
        };
    }
}
