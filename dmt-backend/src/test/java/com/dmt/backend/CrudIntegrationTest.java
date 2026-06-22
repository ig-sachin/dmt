package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class CrudIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldExecuteInsertUpdateDeleteCrudFlow() {

        createDefaultMetadata();

        genericInsertCustomer();
        genericUpdateCustomer();
        genericDeleteCustomer();
    }

    @Test
    void shouldExecuteUpdateProcedure() {

        createDefaultMetadata();

        directProcedureInsertEmployee();
        genericUpdateEmployee();
    }

    @Test
    void shouldExecuteDeleteProcedure() {

        createDefaultMetadata();

        genericInsertOrder();
        directProcedureDeleteOrder();
    }

    @Test
    void shouldDefineUpdateNonExistingRecordBehavior() {

        createDefaultMetadata();

        Map<String, Object> response =
                exchange(HttpMethod.PUT, "/api/data/CUSTOMER", Map.of(
                        "CUSTOMER_ID", 999999,
                        "CUSTOMER_NAME", "Missing Customer",
                        "STATUS", "ACTIVE"
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 999999)).isZero();
    }

    @Test
    void shouldDefineDeleteNonExistingRecordBehavior() {

        createDefaultMetadata();

        Map<String, Object> response =
                exchange(HttpMethod.DELETE, "/api/data/CUSTOMER", Map.of(
                        "CUSTOMER_ID", 999999
                ), Map.class);

        assertThat(response.get("success")).isEqualTo(true);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 999999)).isZero();
    }

    @Test
    void shouldAllowAuthorizedUserAndRejectUnauthorizedUserForCrud() {

        createDefaultMetadata();

        String userToken =
                registerAndLoginUser(
                        "crud_user_" + System.nanoTime(),
                        "password123"
                );

        ResponseEntity<Map> allowedInsert =
                exchangeRawWithToken(
                        userToken,
                        HttpMethod.POST,
                        "/api/data/CUSTOMER",
                        Map.of(
                                "CUSTOMER_ID", 9300,
                                "CUSTOMER_NAME", "RBAC CRUD Customer"
                        )
                );

        assertThat(allowedInsert.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 9300)).isEqualTo(1);

        ResponseEntity<Map> deniedUpdate =
                exchangeRawWithToken(
                        userToken,
                        HttpMethod.PUT,
                        "/api/data/EMPLOYEE",
                        Map.of(
                                "EMPLOYEE_ID", 1,
                                "EMPLOYEE_NAME", "Denied Employee",
                                "DEPARTMENT", "Security",
                                "STATUS", "ACTIVE"
                        )
                );

        assertThat(deniedUpdate.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(deniedUpdate.getBody().get("message").toString())
                .contains("Access denied for screen EMPLOYEE");

        ResponseEntity<Map> deniedDelete =
                exchangeRawWithToken(
                        userToken,
                        HttpMethod.DELETE,
                        "/api/data/EMPLOYEE",
                        Map.of("EMPLOYEE_ID", 1)
                );

        assertThat(deniedDelete.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(deniedDelete.getBody().get("message").toString())
                .contains("Access denied for screen EMPLOYEE");
    }
}