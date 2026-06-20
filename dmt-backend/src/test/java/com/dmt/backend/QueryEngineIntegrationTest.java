package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class QueryEngineIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldSearchCustomer() {

        createDefaultMetadata();

        assertSearch(
                "CUSTOMER",
                "CUSTOMER_ID",
                Map.of(
                        "CUSTOMER_NAME", "Customer",
                        "STATUS", List.of("ACTIVE", "PENDING")
                ),
                25
        );
    }

    @Test
    void shouldSearchBigOrderScreenAndCapPageSize() {

        createDefaultMetadata();

        assertSearch(
                "ORDER_BOOK",
                "ORDER_ID",
                Map.of(
                        "CUSTOMER_NAME", "Customer",
                        "ORDER_STATUS", List.of("NEW", "PROCESSING", "SHIPPED")
                ),
                250,
                100
        );
    }

    @Test
    void shouldRejectInvalidSortColumn() {

        createDefaultMetadata();

        ResponseEntity<Map> response = postRaw("/api/data/CUSTOMER/search", Map.of(
                "page", 0,
                "size", 25,
                "sortColumn", "HACK_COLUMN",
                "sortDirection", "ASC",
                "filters", Map.of()
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString())
                .contains("Invalid sort column");
    }

    @Test
    void shouldRejectInvalidFilterColumn() {

        createDefaultMetadata();

        ResponseEntity<Map> response = postRaw("/api/data/CUSTOMER/search", Map.of(
                "page", 0,
                "size", 25,
                "sortColumn", "CUSTOMER_ID",
                "sortDirection", "ASC",
                "filters", Map.of("HACK_COLUMN", "ABC")
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString())
                .contains("Invalid filter");
    }

    @Test
    void shouldRejectSqlInjectionInSortColumn() {

        createDefaultMetadata();

        ResponseEntity<Map> response = postRaw("/api/data/CUSTOMER/search", Map.of(
                "page", 0,
                "size", 25,
                "sortColumn", "CUSTOMER_ID; DROP TABLE CUSTOMER_MASTER",
                "sortDirection", "ASC",
                "filters", Map.of()
        ));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().get("message").toString())
                .contains("Invalid sort column");
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 1)).isEqualTo(1);
    }

    @Test
    void shouldTreatSqlInjectionTextFilterAsPlainText() {

        createDefaultMetadata();

        Map<String, Object> response = post("/api/data/CUSTOMER/search", Map.of(
                "page", 0,
                "size", 25,
                "sortColumn", "CUSTOMER_ID",
                "sortDirection", "ASC",
                "filters", Map.of("CUSTOMER_NAME", "%' OR 1=1 --")
        ), Map.class);

        assertThat(response.get("content")).asList().isEmpty();
        assertThat(asLong(response.get("totalRecords"))).isZero();
        assertThat(countRows("CUSTOMER_MASTER", "CUSTOMER_ID", 1)).isEqualTo(1);
    }

    @Test
    void shouldReturnEmptySearchResult() {

        createDefaultMetadata();

        Map<String, Object> response = post("/api/data/CUSTOMER/search", Map.of(
                "page", 0,
                "size", 25,
                "sortColumn", "CUSTOMER_ID",
                "sortDirection", "ASC",
                "filters", Map.of("CUSTOMER_NAME", "XYZ_NOT_FOUND")
        ), Map.class);

        assertThat(response.get("content")).asList().isEmpty();
        assertThat(asLong(response.get("totalRecords"))).isZero();
    }

    @Test
    void shouldReturnDifferentRecordsForDifferentPages() {

        createDefaultMetadata();

        Map<String, Object> pageZero = searchCustomerPage(0);
        Map<String, Object> pageOne = searchCustomerPage(1);

        assertThat(pageZero.get("page")).isEqualTo(0);
        assertThat(pageOne.get("page")).isEqualTo(1);
        assertThat(firstCustomerId(pageZero)).isNotEqualTo(firstCustomerId(pageOne));
    }

    @Test
    void shouldAllowAuthorizedUserAndRejectUnauthorizedUserForSearch() {

        createDefaultMetadata();

        String userToken =
                registerAndLoginUser(
                        "query_user_" + System.nanoTime(),
                        "password123"
                );

        ResponseEntity<Map> allowedResponse =
                postRawWithToken(userToken, "/api/data/CUSTOMER/search", Map.of(
                        "page", 0,
                        "size", 10,
                        "sortColumn", "CUSTOMER_ID",
                        "sortDirection", "ASC",
                        "filters", Map.of("CUSTOMER_NAME", "Customer")
                ));

        assertThat(allowedResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allowedResponse.getBody().get("content")).asList().isNotEmpty();

        ResponseEntity<Map> deniedResponse =
                postRawWithToken(userToken, "/api/data/EMPLOYEE/search", Map.of(
                        "page", 0,
                        "size", 10,
                        "sortColumn", "EMPLOYEE_ID",
                        "sortDirection", "ASC",
                        "filters", Map.of("EMPLOYEE_NAME", "Employee")
                ));

        assertThat(deniedResponse.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        assertThat(deniedResponse.getBody().get("message").toString())
                .contains("Access denied for screen EMPLOYEE");
    }

    private Map<String, Object> searchCustomerPage(int page) {

        return post("/api/data/CUSTOMER/search", Map.of(
                "page", page,
                "size", 50,
                "sortColumn", "CUSTOMER_ID",
                "sortDirection", "ASC",
                "filters", Map.of("CUSTOMER_NAME", "Customer")
        ), Map.class);
    }

    private Object firstCustomerId(Map<String, Object> response) {

        List<Map<String, Object>> content =
                (List<Map<String, Object>>) response.get("content");

        assertThat(content).isNotEmpty();

        return content.getFirst().get("CUSTOMER_ID");
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
