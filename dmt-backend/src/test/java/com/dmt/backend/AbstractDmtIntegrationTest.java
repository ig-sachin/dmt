package com.dmt.backend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
abstract class AbstractDmtIntegrationTest {

    @LocalServerPort
    protected int port;

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    protected String token;

    @BeforeEach
    void setUp() {

        prepareOracleObjects();
        token = loginAndGetToken();
    }

    protected TestScreens createDefaultMetadata() {

        long customerScreenId = createCustomerScreen();
        createCustomerColumns(customerScreenId);
        createCustomerFilters(customerScreenId);
        configureCustomerProcedures(customerScreenId);
        createScreenRole(customerScreenId, "ROLE_ADMIN");
        createScreenRole(customerScreenId, "ROLE_USER");

        long employeeScreenId = createEmployeeScreen();
        createEmployeeColumns(employeeScreenId);
        createEmployeeFilters(employeeScreenId);
        configureEmployeeProcedures(employeeScreenId);
        createScreenRole(employeeScreenId, "ROLE_ADMIN");

        long orderScreenId = createOrderScreen();
        createOrderColumns(orderScreenId);
        createOrderFilters(orderScreenId);
        configureOrderProcedures(orderScreenId);
        createScreenRole(orderScreenId, "ROLE_ADMIN");

        return new TestScreens(
                customerScreenId,
                employeeScreenId,
                orderScreenId
        );
    }

    protected record TestScreens(
            long customerScreenId,
            long employeeScreenId,
            long orderScreenId
    ) {
    }

    protected void prepareOracleObjects() {

        clearDmtMetadata();

        dropPackageIfExists("PKG_CUSTOMER");
        dropPackageIfExists("PKG_EMPLOYEE");
        dropPackageIfExists("PKG_ORDER_BOOK");
        dropTableIfExists("CUSTOMER_MASTER");
        dropTableIfExists("EMPLOYEE_MASTER");
        dropTableIfExists("ORDER_BOOK_MASTER");

        jdbcTemplate.execute("""
                CREATE TABLE CUSTOMER_MASTER (
                    CUSTOMER_ID NUMBER PRIMARY KEY,
                    CUSTOMER_NAME VARCHAR2(100) NOT NULL,
                    STATUS VARCHAR2(20) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE EMPLOYEE_MASTER (
                    EMPLOYEE_ID NUMBER PRIMARY KEY,
                    EMPLOYEE_NAME VARCHAR2(100) NOT NULL,
                    DEPARTMENT VARCHAR2(100) NOT NULL,
                    STATUS VARCHAR2(20) NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                CREATE TABLE ORDER_BOOK_MASTER (
                    ORDER_ID NUMBER PRIMARY KEY,
                    CUSTOMER_NAME VARCHAR2(100) NOT NULL,
                    ORDER_STATUS VARCHAR2(20) NOT NULL,
                    REGION VARCHAR2(50) NOT NULL,
                    ORDER_TOTAL NUMBER(12,2) NOT NULL,
                    CREATED_ON DATE NOT NULL
                )
                """);

        jdbcTemplate.execute("""
                BEGIN
                    FOR i IN 1..1000 LOOP
                        INSERT INTO CUSTOMER_MASTER (
                            CUSTOMER_ID,
                            CUSTOMER_NAME,
                            STATUS
                        )
                        VALUES (
                            i,
                            'Customer ' || i,
                            CASE
                                WHEN MOD(i, 3) = 0 THEN 'INACTIVE'
                                WHEN MOD(i, 2) = 0 THEN 'PENDING'
                                ELSE 'ACTIVE'
                            END
                        );
                    END LOOP;
                    COMMIT;
                END;
                """);

        jdbcTemplate.execute("""
                BEGIN
                    FOR i IN 1..500 LOOP
                        INSERT INTO EMPLOYEE_MASTER (
                            EMPLOYEE_ID,
                            EMPLOYEE_NAME,
                            DEPARTMENT,
                            STATUS
                        )
                        VALUES (
                            i,
                            'Employee ' || i,
                            CASE
                                WHEN MOD(i, 4) = 0 THEN 'Finance'
                                WHEN MOD(i, 3) = 0 THEN 'Operations'
                                WHEN MOD(i, 2) = 0 THEN 'Technology'
                                ELSE 'Sales'
                            END,
                            CASE
                                WHEN MOD(i, 5) = 0 THEN 'INACTIVE'
                                ELSE 'ACTIVE'
                            END
                        );
                    END LOOP;
                    COMMIT;
                END;
                """);

        jdbcTemplate.execute("""
                BEGIN
                    FOR i IN 1..2500 LOOP
                        INSERT INTO ORDER_BOOK_MASTER (
                            ORDER_ID,
                            CUSTOMER_NAME,
                            ORDER_STATUS,
                            REGION,
                            ORDER_TOTAL,
                            CREATED_ON
                        )
                        VALUES (
                            i,
                            'Customer ' || MOD(i, 300),
                            CASE
                                WHEN MOD(i, 7) = 0 THEN 'CANCELLED'
                                WHEN MOD(i, 5) = 0 THEN 'SHIPPED'
                                WHEN MOD(i, 3) = 0 THEN 'PROCESSING'
                                ELSE 'NEW'
                            END,
                            CASE
                                WHEN MOD(i, 4) = 0 THEN 'North'
                                WHEN MOD(i, 3) = 0 THEN 'South'
                                WHEN MOD(i, 2) = 0 THEN 'West'
                                ELSE 'East'
                            END,
                            1000 + (i * 12.75),
                            TRUNC(SYSDATE) - MOD(i, 365)
                        );
                    END LOOP;
                    COMMIT;
                END;
                """);

        createCustomerPackage();
        createEmployeePackage();
        createOrderPackage();
    }

    protected void clearDmtMetadata() {

        jdbcTemplate.update("DELETE FROM DMT_AUDIT_LOG");
        jdbcTemplate.update("DELETE FROM DMT_VALIDATION");
        jdbcTemplate.update("DELETE FROM DMT_DROPDOWN_PARAM");
        jdbcTemplate.update("DELETE FROM DMT_DROPDOWN");
        jdbcTemplate.update("DELETE FROM DMT_SCREEN_ROLE");
        jdbcTemplate.update("DELETE FROM DMT_PROCEDURE_PARAM");
        jdbcTemplate.update("DELETE FROM DMT_PROCEDURE");
        jdbcTemplate.update("DELETE FROM DMT_FILTER");
        jdbcTemplate.update("DELETE FROM DMT_COLUMN");
        jdbcTemplate.update("DELETE FROM DMT_SCREEN");
    }

    protected void createCustomerPackage() {

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE PKG_CUSTOMER AS
                    PROCEDURE INSERT_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    );

                    PROCEDURE UPDATE_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    );

                    PROCEDURE DELETE_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER
                    );
                END PKG_CUSTOMER;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE BODY PKG_CUSTOMER AS
                    PROCEDURE INSERT_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    ) AS
                    BEGIN
                        INSERT INTO CUSTOMER_MASTER (
                            CUSTOMER_ID,
                            CUSTOMER_NAME,
                            STATUS
                        )
                        VALUES (
                            P_CUSTOMER_ID,
                            P_CUSTOMER_NAME,
                            P_STATUS
                        );
                        COMMIT;
                    END INSERT_CUSTOMER;

                    PROCEDURE UPDATE_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    ) AS
                    BEGIN
                        UPDATE CUSTOMER_MASTER
                        SET CUSTOMER_NAME = P_CUSTOMER_NAME,
                            STATUS = P_STATUS
                        WHERE CUSTOMER_ID = P_CUSTOMER_ID;
                        COMMIT;
                    END UPDATE_CUSTOMER;

                    PROCEDURE DELETE_CUSTOMER(
                        P_CUSTOMER_ID IN NUMBER
                    ) AS
                    BEGIN
                        DELETE FROM CUSTOMER_MASTER
                        WHERE CUSTOMER_ID = P_CUSTOMER_ID;
                        COMMIT;
                    END DELETE_CUSTOMER;
                END PKG_CUSTOMER;
                """);
    }

    protected void createEmployeePackage() {

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE PKG_EMPLOYEE AS
                    PROCEDURE INSERT_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER,
                        P_EMPLOYEE_NAME IN VARCHAR2,
                        P_DEPARTMENT IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    );

                    PROCEDURE UPDATE_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER,
                        P_EMPLOYEE_NAME IN VARCHAR2,
                        P_DEPARTMENT IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    );

                    PROCEDURE DELETE_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER
                    );
                END PKG_EMPLOYEE;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE BODY PKG_EMPLOYEE AS
                    PROCEDURE INSERT_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER,
                        P_EMPLOYEE_NAME IN VARCHAR2,
                        P_DEPARTMENT IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    ) AS
                    BEGIN
                        INSERT INTO EMPLOYEE_MASTER (
                            EMPLOYEE_ID,
                            EMPLOYEE_NAME,
                            DEPARTMENT,
                            STATUS
                        )
                        VALUES (
                            P_EMPLOYEE_ID,
                            P_EMPLOYEE_NAME,
                            P_DEPARTMENT,
                            P_STATUS
                        );
                        COMMIT;
                    END INSERT_EMPLOYEE;

                    PROCEDURE UPDATE_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER,
                        P_EMPLOYEE_NAME IN VARCHAR2,
                        P_DEPARTMENT IN VARCHAR2,
                        P_STATUS IN VARCHAR2
                    ) AS
                    BEGIN
                        UPDATE EMPLOYEE_MASTER
                        SET EMPLOYEE_NAME = P_EMPLOYEE_NAME,
                            DEPARTMENT = P_DEPARTMENT,
                            STATUS = P_STATUS
                        WHERE EMPLOYEE_ID = P_EMPLOYEE_ID;
                        COMMIT;
                    END UPDATE_EMPLOYEE;

                    PROCEDURE DELETE_EMPLOYEE(
                        P_EMPLOYEE_ID IN NUMBER
                    ) AS
                    BEGIN
                        DELETE FROM EMPLOYEE_MASTER
                        WHERE EMPLOYEE_ID = P_EMPLOYEE_ID;
                        COMMIT;
                    END DELETE_EMPLOYEE;
                END PKG_EMPLOYEE;
                """);
    }

    protected void createOrderPackage() {

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE PKG_ORDER_BOOK AS
                    PROCEDURE INSERT_ORDER(
                        P_ORDER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_ORDER_STATUS IN VARCHAR2,
                        P_REGION IN VARCHAR2,
                        P_ORDER_TOTAL IN NUMBER
                    );

                    PROCEDURE UPDATE_ORDER(
                        P_ORDER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_ORDER_STATUS IN VARCHAR2,
                        P_REGION IN VARCHAR2,
                        P_ORDER_TOTAL IN NUMBER
                    );

                    PROCEDURE DELETE_ORDER(
                        P_ORDER_ID IN NUMBER
                    );
                END PKG_ORDER_BOOK;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE BODY PKG_ORDER_BOOK AS
                    PROCEDURE INSERT_ORDER(
                        P_ORDER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_ORDER_STATUS IN VARCHAR2,
                        P_REGION IN VARCHAR2,
                        P_ORDER_TOTAL IN NUMBER
                    ) AS
                    BEGIN
                        INSERT INTO ORDER_BOOK_MASTER (
                            ORDER_ID,
                            CUSTOMER_NAME,
                            ORDER_STATUS,
                            REGION,
                            ORDER_TOTAL,
                            CREATED_ON
                        )
                        VALUES (
                            P_ORDER_ID,
                            P_CUSTOMER_NAME,
                            P_ORDER_STATUS,
                            P_REGION,
                            P_ORDER_TOTAL,
                            TRUNC(SYSDATE)
                        );
                        COMMIT;
                    END INSERT_ORDER;

                    PROCEDURE UPDATE_ORDER(
                        P_ORDER_ID IN NUMBER,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_ORDER_STATUS IN VARCHAR2,
                        P_REGION IN VARCHAR2,
                        P_ORDER_TOTAL IN NUMBER
                    ) AS
                    BEGIN
                        UPDATE ORDER_BOOK_MASTER
                        SET CUSTOMER_NAME = P_CUSTOMER_NAME,
                            ORDER_STATUS = P_ORDER_STATUS,
                            REGION = P_REGION,
                            ORDER_TOTAL = P_ORDER_TOTAL
                        WHERE ORDER_ID = P_ORDER_ID;
                        COMMIT;
                    END UPDATE_ORDER;

                    PROCEDURE DELETE_ORDER(
                        P_ORDER_ID IN NUMBER
                    ) AS
                    BEGIN
                        DELETE FROM ORDER_BOOK_MASTER
                        WHERE ORDER_ID = P_ORDER_ID;
                        COMMIT;
                    END DELETE_ORDER;
                END PKG_ORDER_BOOK;
                """);
    }

    protected long createCustomerScreen() {

        return createScreen(Map.of(
                "screenCode", "CUSTOMER",
                "screenName", "Customer Management",
                "description", "Customer management integration test screen",
                "selectQuery", "SELECT CUSTOMER_ID, CUSTOMER_NAME, STATUS FROM CUSTOMER_MASTER",
                "defaultPageSize", 25,
                "defaultSortColumn", "CUSTOMER_ID",
                "defaultSortDirection", "ASC",
                "primaryKeyColumn", "CUSTOMER_ID",
                "active", true
        ));
    }

    protected long createEmployeeScreen() {

        return createScreen(Map.of(
                "screenCode", "EMPLOYEE",
                "screenName", "Employee Management",
                "description", "Employee management integration test screen",
                "selectQuery", "SELECT EMPLOYEE_ID, EMPLOYEE_NAME, DEPARTMENT, STATUS FROM EMPLOYEE_MASTER",
                "defaultPageSize", 30,
                "defaultSortColumn", "EMPLOYEE_ID",
                "defaultSortDirection", "ASC",
                "primaryKeyColumn", "EMPLOYEE_ID",
                "active", true
        ));
    }

    protected long createOrderScreen() {

        return createScreen(Map.of(
                "screenCode", "ORDER_BOOK",
                "screenName", "Order Book",
                "description", "Large order book integration test screen",
                "selectQuery", """
                        SELECT ORDER_ID,
                               CUSTOMER_NAME,
                               ORDER_STATUS,
                               REGION,
                               ORDER_TOTAL,
                               CREATED_ON
                        FROM ORDER_BOOK_MASTER
                        """,
                "defaultPageSize", 75,
                "defaultSortColumn", "ORDER_ID",
                "defaultSortDirection", "ASC",
                "primaryKeyColumn", "ORDER_ID",
                "active", true
        ));
    }

    protected long createScreen(Map<String, Object> body) {

        Map<String, Object> response =
                post("/api/screens", body, Map.class);

        return asLong(response.get("id"));
    }

    protected long createScreenRole(
            long screenId,
            String roleName) {

        Map<String, Object> response =
                post("/api/screen-roles", Map.of(
                        "screenId", screenId,
                        "roleName", roleName
                ), Map.class);

        return asLong(response.get("id"));
    }

    protected void createCustomerColumns(long screenId) {

        createColumn(screenId, "CUSTOMER_ID", "Customer ID", "NUMBER", "NUMBER", false, 1);
        createColumn(screenId, "CUSTOMER_NAME", "Customer Name", "VARCHAR2", "TEXT", true, 2);
        createColumn(screenId, "STATUS", "Status", "VARCHAR2", "DROPDOWN", true, 3);
    }

    protected void createEmployeeColumns(long screenId) {

        createColumn(screenId, "EMPLOYEE_ID", "Employee ID", "NUMBER", "NUMBER", false, 1);
        createColumn(screenId, "EMPLOYEE_NAME", "Employee Name", "VARCHAR2", "TEXT", true, 2);
        createColumn(screenId, "DEPARTMENT", "Department", "VARCHAR2", "TEXT", true, 3);
        createColumn(screenId, "STATUS", "Status", "VARCHAR2", "DROPDOWN", true, 4);
    }

    protected void createOrderColumns(long screenId) {

        createColumn(screenId, "ORDER_ID", "Order ID", "NUMBER", "NUMBER", false, 1);
        createColumn(screenId, "CUSTOMER_NAME", "Customer Name", "VARCHAR2", "TEXT", true, 2);
        createColumn(screenId, "ORDER_STATUS", "Order Status", "VARCHAR2", "DROPDOWN", true, 3);
        createColumn(screenId, "REGION", "Region", "VARCHAR2", "DROPDOWN", true, 4);
        createColumn(screenId, "ORDER_TOTAL", "Order Total", "NUMBER", "NUMBER", true, 5);
        createColumn(screenId, "CREATED_ON", "Created On", "DATE", "DATE", false, 6);
    }

    protected void createColumn(
            long screenId,
            String columnName,
            String displayName,
            String dataType,
            String fieldType,
            boolean editable,
            int displayOrder) {

        post("/api/columns", mutableMap(
                "screenId", screenId,
                "columnName", columnName,
                "displayName", displayName,
                "dataType", dataType,
                "fieldType", fieldType,
                "visible", true,
                "editable", editable,
                "mandatory", true,
                "displayOrder", displayOrder,
                "width", 150,
                "alignment", "LEFT",
                "placeHolder", displayName,
                "maxLength", 100,
                "dropdownCode", fieldType.equals("DROPDOWN") ? columnName + "_DROPDOWN" : null
        ), Map.class);
    }

    protected void createCustomerFilters(long screenId) {

        createFilter(screenId, "Customer Name", "CUSTOMER_NAME", "TEXT", 1);
        createFilter(screenId, "Status", "STATUS", "MULTI_SELECT", 2);
    }

    protected void createEmployeeFilters(long screenId) {

        createFilter(screenId, "Employee Name", "EMPLOYEE_NAME", "TEXT", 1);
        createFilter(screenId, "Status", "STATUS", "MULTI_SELECT", 2);
    }

    protected void createOrderFilters(long screenId) {

        createFilter(screenId, "Customer Name", "CUSTOMER_NAME", "TEXT", 1);
        createFilter(screenId, "Order Status", "ORDER_STATUS", "MULTI_SELECT", 2);
        createFilter(screenId, "Region", "REGION", "DROPDOWN", 3);
    }

    protected void createFilter(
            long screenId,
            String filterName,
            String columnName,
            String filterType,
            int displayOrder) {

        post("/api/filters", Map.of(
                "screenId", screenId,
                "filterName", filterName,
                "columnName", columnName,
                "filterType", filterType,
                "required", false,
                "displayOrder", displayOrder
        ), Map.class);
    }

    protected void configureCustomerProcedures(long screenId) {

        long insertProcedureId = createProcedure(screenId, "INSERT", "PKG_CUSTOMER.INSERT_CUSTOMER");
        long updateProcedureId = createProcedure(screenId, "UPDATE", "PKG_CUSTOMER.UPDATE_CUSTOMER");
        long deleteProcedureId = createProcedure(screenId, "DELETE", "PKG_CUSTOMER.DELETE_CUSTOMER");

        createProcedureParam(insertProcedureId, "P_CUSTOMER_ID", 1, "CUSTOMER_ID", null, true);
        createProcedureParam(insertProcedureId, "P_CUSTOMER_NAME", 2, "CUSTOMER_NAME", null, true);
        createProcedureParam(insertProcedureId, "P_STATUS", 3, "STATUS", "ACTIVE", false);

        createProcedureParam(updateProcedureId, "P_CUSTOMER_ID", 1, "CUSTOMER_ID", null, true);
        createProcedureParam(updateProcedureId, "P_CUSTOMER_NAME", 2, "CUSTOMER_NAME", null, true);
        createProcedureParam(updateProcedureId, "P_STATUS", 3, "STATUS", null, true);

        createProcedureParam(deleteProcedureId, "P_CUSTOMER_ID", 1, "CUSTOMER_ID", null, true);
    }

    protected void configureEmployeeProcedures(long screenId) {

        long insertProcedureId = createProcedure(screenId, "INSERT", "PKG_EMPLOYEE.INSERT_EMPLOYEE");
        long updateProcedureId = createProcedure(screenId, "UPDATE", "PKG_EMPLOYEE.UPDATE_EMPLOYEE");
        long deleteProcedureId = createProcedure(screenId, "DELETE", "PKG_EMPLOYEE.DELETE_EMPLOYEE");

        createProcedureParam(insertProcedureId, "P_EMPLOYEE_ID", 1, "EMPLOYEE_ID", null, true);
        createProcedureParam(insertProcedureId, "P_EMPLOYEE_NAME", 2, "EMPLOYEE_NAME", null, true);
        createProcedureParam(insertProcedureId, "P_DEPARTMENT", 3, "DEPARTMENT", "Technology", false);
        createProcedureParam(insertProcedureId, "P_STATUS", 4, "STATUS", "ACTIVE", false);

        createProcedureParam(updateProcedureId, "P_EMPLOYEE_ID", 1, "EMPLOYEE_ID", null, true);
        createProcedureParam(updateProcedureId, "P_EMPLOYEE_NAME", 2, "EMPLOYEE_NAME", null, true);
        createProcedureParam(updateProcedureId, "P_DEPARTMENT", 3, "DEPARTMENT", null, true);
        createProcedureParam(updateProcedureId, "P_STATUS", 4, "STATUS", null, true);

        createProcedureParam(deleteProcedureId, "P_EMPLOYEE_ID", 1, "EMPLOYEE_ID", null, true);
    }

    protected void configureOrderProcedures(long screenId) {

        long insertProcedureId = createProcedure(screenId, "INSERT", "PKG_ORDER_BOOK.INSERT_ORDER");
        long updateProcedureId = createProcedure(screenId, "UPDATE", "PKG_ORDER_BOOK.UPDATE_ORDER");
        long deleteProcedureId = createProcedure(screenId, "DELETE", "PKG_ORDER_BOOK.DELETE_ORDER");

        createProcedureParam(insertProcedureId, "P_ORDER_ID", 1, "ORDER_ID", null, true);
        createProcedureParam(insertProcedureId, "P_CUSTOMER_NAME", 2, "CUSTOMER_NAME", null, true);
        createProcedureParam(insertProcedureId, "P_ORDER_STATUS", 3, "ORDER_STATUS", "NEW", false);
        createProcedureParam(insertProcedureId, "P_REGION", 4, "REGION", "East", false);
        createProcedureParam(insertProcedureId, "P_ORDER_TOTAL", 5, "ORDER_TOTAL", "0", false);

        createProcedureParam(updateProcedureId, "P_ORDER_ID", 1, "ORDER_ID", null, true);
        createProcedureParam(updateProcedureId, "P_CUSTOMER_NAME", 2, "CUSTOMER_NAME", null, true);
        createProcedureParam(updateProcedureId, "P_ORDER_STATUS", 3, "ORDER_STATUS", null, true);
        createProcedureParam(updateProcedureId, "P_REGION", 4, "REGION", null, true);
        createProcedureParam(updateProcedureId, "P_ORDER_TOTAL", 5, "ORDER_TOTAL", null, true);

        createProcedureParam(deleteProcedureId, "P_ORDER_ID", 1, "ORDER_ID", null, true);
    }

    protected long createProcedure(
            long screenId,
            String operationType,
            String procedureName) {

        Map<String, Object> response = post("/api/procedures", Map.of(
                "screenId", screenId,
                "operationType", operationType,
                "procedureName", procedureName,
                "active", true
        ), Map.class);

        return asLong(response.get("id"));
    }

    protected void createProcedureParam(
            long procedureId,
            String parameterName,
            int parameterOrder,
            String columnName,
            String defaultValue,
            boolean required) {

        post("/api/procedure-params", mutableMap(
                "procedureId", procedureId,
                "parameterName", parameterName,
                "parameterOrder", parameterOrder,
                "columnName", columnName,
                "defaultValue", defaultValue,
                "required", required
        ), Map.class);
    }

    protected long createDropdown(
            String dropdownCode,
            String dropdownName,
            String query) {

        Map<String, Object> response =
                post("/api/dropdowns", Map.of(
                        "dropdownCode", dropdownCode,
                        "dropdownName", dropdownName,
                        "query", query,
                        "active", true
                ), Map.class);

        return asLong(response.get("id"));
    }

    protected long createDropdownParam(
            long dropdownId,
            String parameterName,
            String requestField,
            boolean required,
            int parameterOrder) {

        Map<String, Object> response =
                post("/api/dropdown-params", Map.of(
                        "dropdownId", dropdownId,
                        "parameterName", parameterName,
                        "requestField", requestField,
                        "required", required,
                        "parameterOrder", parameterOrder
                ), Map.class);

        return asLong(response.get("id"));
    }

    protected long createValidation(
            long columnId,
            String validationType,
            String validationValue,
            String errorMessage) {

        Map<String, Object> response =
                post("/api/validations", mutableMap(
                        "columnId", columnId,
                        "validationType", validationType,
                        "validationValue", validationValue,
                        "errorMessage", errorMessage,
                        "active", true
                ), Map.class);

        return asLong(response.get("id"));
    }

    protected void assertMetadata(
            String screenCode,
            int expectedColumnCount,
            int expectedFilterCount) {

        Map<String, Object> metadata =
                get("/api/metadata/" + screenCode, Map.class);

        assertThat(metadata.get("screen")).isNotNull();
        assertThat((List<?>) metadata.get("columns")).hasSize(expectedColumnCount);
        assertThat((List<?>) metadata.get("filters")).hasSize(expectedFilterCount);
    }

    protected void assertScreenLookup(
            long screenId,
            String expectedScreenCode) {

        Map<String, Object> screen =
                get("/api/screens/" + screenId, Map.class);

        assertThat(screen.get("screenCode")).isEqualTo(expectedScreenCode);
    }

    protected void assertAllScreensContain(String... screenCodes) {

        List<Map<String, Object>> screens =
                get("/api/screens", List.class);

        assertThat(screens)
                .extracting(screen -> screen.get("screenCode"))
                .contains((Object[]) screenCodes);
    }

    protected void assertMetadataChildrenCanBeListed(
            long screenId,
            int expectedColumnCount,
            int expectedFilterCount,
            int expectedProcedureCount) {

        List<?> columns =
                get("/api/columns/screen/" + screenId, List.class);
        List<?> filters =
                get("/api/filters/screen/" + screenId, List.class);
        List<Map<String, Object>> procedures =
                get("/api/procedures/screen/" + screenId, List.class);

        assertThat(columns).hasSize(expectedColumnCount);
        assertThat(filters).hasSize(expectedFilterCount);
        assertThat(procedures).hasSize(expectedProcedureCount);

        procedures.forEach(procedure -> {
            long procedureId = asLong(procedure.get("id"));

            assertThat(get(
                    "/api/procedure-params/procedure/" + procedureId,
                    List.class
            )).isNotEmpty();
        });
    }

    protected void assertSearch(
            String screenCode,
            String sortColumn,
            Map<String, Object> filters,
            int size) {

        assertSearch(screenCode, sortColumn, filters, size, size);
    }

    protected void assertSearch(
            String screenCode,
            String sortColumn,
            Map<String, Object> filters,
            int requestedSize,
            int expectedReturnedSize) {

        Map<String, Object> response =
                post("/api/data/" + screenCode + "/search", Map.of(
                        "page", 0,
                        "size", requestedSize,
                        "sortColumn", sortColumn,
                        "sortDirection", "ASC",
                        "filters", filters
                ), Map.class);

        assertThat(asLong(response.get("totalRecords"))).isGreaterThan(0);
        assertThat((List<?>) response.get("content"))
                .isNotEmpty()
                .hasSizeLessThanOrEqualTo(expectedReturnedSize);
        assertThat(asLong(response.get("size"))).isEqualTo(expectedReturnedSize);
    }

    protected void genericInsertCustomer() {

        Map<String, Object> response =
                post("/api/data/CUSTOMER", Map.of(
                        "CUSTOMER_ID", 2001,
                        "CUSTOMER_NAME", "Integration Customer",
                        "STATUS", "ACTIVE"
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 2001)).isEqualTo(1);
    }

    protected void genericUpdateCustomer() {

        Map<String, Object> response =
                exchange(HttpMethod.PUT, "/api/data/CUSTOMER", Map.of(
                        "CUSTOMER_ID", 2001,
                        "CUSTOMER_NAME", "Integration Customer Updated",
                        "STATUS", "INACTIVE"
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(queryString(
                "SELECT STATUS FROM CUSTOMER_MASTER WHERE CUSTOMER_ID = 2001"
        )).isEqualTo("INACTIVE");
    }

    protected void genericDeleteCustomer() {

        Map<String, Object> response =
                exchange(HttpMethod.DELETE, "/api/data/CUSTOMER", Map.of(
                        "CUSTOMER_ID", 2001
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 2001)).isZero();
    }

    protected void directProcedureInsertEmployee() {

        Map<String, Object> response =
                post("/api/procedure-engine/execute", Map.of(
                        "screenCode", "EMPLOYEE",
                        "operationType", "INSERT",
                        "values", Map.of(
                                "EMPLOYEE_ID", 3001,
                                "EMPLOYEE_NAME", "Integration Employee",
                                "DEPARTMENT", "Technology",
                                "STATUS", "ACTIVE"
                        )
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("EMPLOYEE_MASTER", "EMPLOYEE_ID", 3001)).isEqualTo(1);
    }

    protected void genericUpdateEmployee() {

        Map<String, Object> response =
                exchange(HttpMethod.PUT, "/api/data/EMPLOYEE", Map.of(
                        "EMPLOYEE_ID", 3001,
                        "EMPLOYEE_NAME", "Integration Employee Updated",
                        "DEPARTMENT", "Operations",
                        "STATUS", "INACTIVE"
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(queryString(
                "SELECT DEPARTMENT FROM EMPLOYEE_MASTER WHERE EMPLOYEE_ID = 3001"
        )).isEqualTo("Operations");
    }

    protected void genericDeleteEmployee() {

        Map<String, Object> response =
                exchange(HttpMethod.DELETE, "/api/data/EMPLOYEE", Map.of(
                        "EMPLOYEE_ID", 3001
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("EMPLOYEE_MASTER", "EMPLOYEE_ID", 3001)).isZero();
    }

    protected void genericInsertOrder() {

        Map<String, Object> response =
                post("/api/data/ORDER_BOOK", Map.of(
                        "ORDER_ID", 9001,
                        "CUSTOMER_NAME", "Integration Order Customer",
                        "ORDER_STATUS", "NEW",
                        "REGION", "North",
                        "ORDER_TOTAL", 4250.75
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("ORDER_BOOK_MASTER", "ORDER_ID", 9001)).isEqualTo(1);
    }

    protected void genericUpdateOrder() {

        Map<String, Object> response =
                exchange(HttpMethod.PUT, "/api/data/ORDER_BOOK", Map.of(
                        "ORDER_ID", 9001,
                        "CUSTOMER_NAME", "Integration Order Customer Updated",
                        "ORDER_STATUS", "SHIPPED",
                        "REGION", "South",
                        "ORDER_TOTAL", 5250.25
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(queryString(
                "SELECT ORDER_STATUS FROM ORDER_BOOK_MASTER WHERE ORDER_ID = 9001"
        )).isEqualTo("SHIPPED");
    }

    protected void directProcedureDeleteOrder() {

        Map<String, Object> response =
                post("/api/procedure-engine/execute", Map.of(
                        "screenCode", "ORDER_BOOK",
                        "operationType", "DELETE",
                        "values", Map.of(
                                "ORDER_ID", 9001
                        )
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("ORDER_BOOK_MASTER", "ORDER_ID", 9001)).isZero();
    }

    protected void assertMetadataDeleteEndpoints() {

        long screenId = createScreen(Map.of(
                "screenCode", "DELETE_ME",
                "screenName", "Delete Me",
                "description", "Temporary screen used to verify delete endpoints",
                "selectQuery", "SELECT CUSTOMER_ID FROM CUSTOMER_MASTER",
                "defaultPageSize", 10,
                "defaultSortColumn", "CUSTOMER_ID",
                "defaultSortDirection", "ASC",
                "active", true
        ));

        Map<String, Object> column =
                post("/api/columns", mutableMap(
                        "screenId", screenId,
                        "columnName", "CUSTOMER_ID",
                        "displayName", "Customer ID",
                        "dataType", "NUMBER",
                        "fieldType", "NUMBER",
                        "visible", true,
                        "editable", false,
                        "mandatory", true,
                        "displayOrder", 1,
                        "width", 120,
                        "alignment", "LEFT"
                ), Map.class);

        Map<String, Object> filter =
                post("/api/filters", Map.of(
                        "screenId", screenId,
                        "filterName", "Customer ID",
                        "columnName", "CUSTOMER_ID",
                        "filterType", "NUMBER",
                        "required", false,
                        "displayOrder", 1
                ), Map.class);

        Map<String, Object> procedure =
                post("/api/procedures", Map.of(
                        "screenId", screenId,
                        "operationType", "DELETE",
                        "procedureName", "PKG_CUSTOMER.DELETE_CUSTOMER",
                        "active", true
                ), Map.class);

        exchange(HttpMethod.DELETE, "/api/columns/" + asLong(column.get("id")), null, Void.class);
        exchange(HttpMethod.DELETE, "/api/filters/" + asLong(filter.get("id")), null, Void.class);
        exchange(HttpMethod.DELETE, "/api/procedures/" + asLong(procedure.get("id")), null, Void.class);
        exchange(HttpMethod.DELETE, "/api/screens/" + screenId, null, Void.class);
    }

    protected String loginAndGetToken() {

        Map<String, Object> response =
                restTemplate.postForObject(
                        url("/auth/login"),
                        Map.of(
                                "username", "sachin",
                                "password", "sachin123"
                        ),
                        Map.class
                );

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isNotNull();

        return response.get("token").toString();
    }

    protected String registerAndLoginUser(
            String username,
            String password) {

        restTemplate.postForEntity(
                url("/auth/register"),
                Map.of(
                        "username", username,
                        "password", password
                ),
                String.class
        );

        Map<String, Object> response =
                restTemplate.postForObject(
                        url("/auth/login"),
                        Map.of(
                                "username", username,
                                "password", password
                        ),
                        Map.class
                );

        assertThat(response).isNotNull();
        assertThat(response.get("token")).isNotNull();

        return response.get("token").toString();
    }

    protected <T> T get(String path, Class<T> responseType) {

        return exchange(HttpMethod.GET, path, null, responseType);
    }

    protected <T> T post(
            String path,
            Object body,
            Class<T> responseType) {

        return exchange(HttpMethod.POST, path, body, responseType);
    }

    protected <T> T exchange(
            HttpMethod method,
            String path,
            Object body,
            Class<T> responseType) {

        ResponseEntity<T> response = restTemplate.exchange(
                url(path),
                method,
                new HttpEntity<>(body, headers()),
                responseType
        );

        assertThat(response.getStatusCode()).isIn(
                HttpStatus.OK,
                HttpStatus.CREATED,
                HttpStatus.NO_CONTENT
        );

        return response.getBody();
    }

    protected HttpHeaders headers() {

        return headers(token);
    }

    protected HttpHeaders headers(String bearerToken) {

        HttpHeaders headers = new HttpHeaders();

        headers.setBearerAuth(bearerToken);
        headers.setContentType(MediaType.APPLICATION_JSON);

        return headers;
    }

    protected String url(String path) {

        return "http://localhost:" + port + path;
    }

    protected long asLong(Object value) {

        assertThat(value).isInstanceOf(Number.class);

        return ((Number) value).longValue();
    }

    protected int countRows(
            String tableName,
            String idColumn,
            int id) {

        Integer count = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM " + tableName + " WHERE " + idColumn + " = ?",
                Integer.class,
                id
        );

        return count == null ? 0 : count;
    }

    protected String queryString(String sql) {

        return jdbcTemplate.queryForObject(sql, String.class);
    }

    protected void dropTableIfExists(String tableName) {

        jdbcTemplate.execute("""
                BEGIN
                    EXECUTE IMMEDIATE 'DROP TABLE %s PURGE';
                EXCEPTION
                    WHEN OTHERS THEN
                        IF SQLCODE != -942 THEN
                            RAISE;
                        END IF;
                END;
                """.formatted(tableName));
    }

    protected void dropPackageIfExists(String packageName) {

        jdbcTemplate.execute("""
                BEGIN
                    EXECUTE IMMEDIATE 'DROP PACKAGE %s';
                EXCEPTION
                    WHEN OTHERS THEN
                        IF SQLCODE != -4043 THEN
                            RAISE;
                        END IF;
                END;
                """.formatted(packageName));
    }

    protected Map<String, Object> mutableMap(Object... values) {

        java.util.LinkedHashMap<String, Object> map =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < values.length; i += 2) {
            map.put(values[i].toString(), values[i + 1]);
        }

        return map;
    }
}
