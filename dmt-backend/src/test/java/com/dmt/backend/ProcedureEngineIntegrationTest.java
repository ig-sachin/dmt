package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ProcedureEngineIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldExecuteInsertProcedure() {

        createDefaultMetadata();

        directProcedureInsertEmployee();
    }

    @Test
    void shouldUseDefaultProcedureParamValue() {

        createDefaultMetadata();

        Map<String, Object> response = post("/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9000,
                "CUSTOMER_NAME", "Test"
        ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(queryString(
                "SELECT STATUS FROM CUSTOMER_MASTER WHERE CUSTOMER_ID = 9000"
        )).isEqualTo("ACTIVE");
    }

    @Test
    void shouldRejectMissingRequiredProcedureParam() {

        createDefaultMetadata();

        ResponseEntity<Map> response = postRaw("/api/data/CUSTOMER", Map.of(
                "CUSTOMER_NAME", "Sachin"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString())
                .contains("Missing required value for: CUSTOMER_ID");
    }

    @Test
    void shouldReturnProcedureNotFoundWhenMappingIsDeleted() {

        createDefaultMetadata();

        jdbcTemplate.update("""
                DELETE FROM DMT_PROCEDURE_PARAM
                WHERE PROCEDURE_ID IN (
                    SELECT ID FROM DMT_PROCEDURE
                    WHERE OPERATION_TYPE = 'INSERT'
                )
                """);
        jdbcTemplate.update("""
                DELETE FROM DMT_PROCEDURE
                WHERE OPERATION_TYPE = 'INSERT'
                """);

        ResponseEntity<Map> response = postRaw("/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9002,
                "CUSTOMER_NAME", "Missing Mapping"
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString())
                .contains("Procedure not found");
    }
// TODO: Failing  shouldBindProcedureParamsByParameterOrder failing with 2026-06-20 16:37:16.528 WARN  [b776a779-7a27-4776-af16-72477c664b1d] [http-nio-auto-1-exec-5] c.d.b.c.e.GlobalExceptionHandler - Access denied path=/api/data/CUSTOMER message=Access denied for screen CUSTOMER
    @Test
    void shouldBindProcedureParamsByParameterOrder() {

        long screenId = createCustomerScreen();
        createCustomerColumns(screenId);
        createCustomerFilters(screenId);
        createReverseCustomerPackage();
        createScreenRole(screenId, "ROLE_ADMIN");

        long insertProcedureId =
                createProcedure(screenId, "INSERT", "PKG_CUSTOMER_REVERSE.INSERT_CUSTOMER_REVERSE");

        createProcedureParam(insertProcedureId, "P_CUSTOMER_NAME", 2, "CUSTOMER_NAME", null, true);
        createProcedureParam(insertProcedureId, "P_CUSTOMER_ID", 3, "CUSTOMER_ID", null, true);
        createProcedureParam(insertProcedureId, "P_STATUS", 1, "STATUS", null, true);

        Map<String, Object> response = post("/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9100,
                "CUSTOMER_NAME", "Ordered Bind",
                "STATUS", "PENDING"
        ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(queryString(
                "SELECT CUSTOMER_NAME FROM CUSTOMER_MASTER WHERE CUSTOMER_ID = 9100"
        )).isEqualTo("Ordered Bind");
        assertThat(queryString(
                "SELECT STATUS FROM CUSTOMER_MASTER WHERE CUSTOMER_ID = 9100"
        )).isEqualTo("PENDING");
    }

    @Test
    void shouldAllowAuthorizedUserAndRejectUnauthorizedUserForProcedureExecution() {

        createDefaultMetadata();

        String userToken =
                registerAndLoginUser(
                        "procedure_user_" + System.nanoTime(),
                        "password123"
                );

        ResponseEntity<Map> allowedResponse =
                postRawWithToken(userToken, "/api/procedure-engine/execute", Map.of(
                        "screenCode", "CUSTOMER",
                        "operationType", "INSERT",
                        "values", Map.of(
                                "CUSTOMER_ID", 9200,
                                "CUSTOMER_NAME", "RBAC Customer"
                        )
                ));

        assertThat(allowedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 9200)).isEqualTo(1);

        ResponseEntity<Map> deniedResponse =
                postRawWithToken(userToken, "/api/procedure-engine/execute", Map.of(
                        "screenCode", "EMPLOYEE",
                        "operationType", "INSERT",
                        "values", Map.of(
                                "EMPLOYEE_ID", 9200,
                                "EMPLOYEE_NAME", "RBAC Employee"
                        )
                ));

        assertThat(deniedResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(deniedResponse.getBody().get("message").toString())
                .contains("Access denied for screen EMPLOYEE");
    }

    private void createReverseCustomerPackage() {

        dropPackageIfExists("PKG_CUSTOMER_REVERSE");

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE PKG_CUSTOMER_REVERSE AS
                    PROCEDURE INSERT_CUSTOMER_REVERSE(
                        P_STATUS IN VARCHAR2,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_CUSTOMER_ID IN NUMBER
                    );
                END PKG_CUSTOMER_REVERSE;
                """);

        jdbcTemplate.execute("""
                CREATE OR REPLACE PACKAGE BODY PKG_CUSTOMER_REVERSE AS
                    PROCEDURE INSERT_CUSTOMER_REVERSE(
                        P_STATUS IN VARCHAR2,
                        P_CUSTOMER_NAME IN VARCHAR2,
                        P_CUSTOMER_ID IN NUMBER
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
                    END INSERT_CUSTOMER_REVERSE;
                END PKG_CUSTOMER_REVERSE;
                """);
    }

    private ResponseEntity<Map> postRaw(
            String path,
            Object body) {

        return postRawWithToken(token, path, body);
    }

    private ResponseEntity<Map> postRawWithToken(
            String bearerToken,
            String path,
            Object body) {

        return restTemplate.exchange(
                url(path),
                HttpMethod.POST,
                new HttpEntity<>(body, headers(bearerToken)),
                Map.class
        );
    }
}
