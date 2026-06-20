package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MetadataEngineIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldReturnFormMetadataWithValidationsAndDropdownMetadata() {

        TestScreens screens = createDefaultMetadata();
        long customerNameColumnId =
                findColumnId(screens.customerScreenId(), "CUSTOMER_NAME");

        createValidation(
                customerNameColumnId,
                "MIN_LENGTH",
                "3",
                "Customer name must be at least 3 characters"
        );

        Map<String, Object> response =
                get("/api/forms/CUSTOMER", Map.class);

        assertThat(response.get("screenCode")).isEqualTo("CUSTOMER");
        assertThat(response.get("fields")).asList().hasSize(3);

        Map<String, Object> customerNameField =
                findField(response, "CUSTOMER_NAME");
        Map<String, Object> statusField =
                findField(response, "STATUS");

        assertThat(customerNameField.get("fieldType")).isEqualTo("TEXT");
        assertThat(customerNameField.get("mandatory")).isEqualTo(true);
        assertThat(customerNameField.get("placeholder")).isEqualTo("Customer Name");
        assertThat(customerNameField.get("maxLength")).isEqualTo(100);
        assertThat(customerNameField.get("validations")).asList().isNotEmpty();
        assertThat(statusField.get("dropdownCode")).isEqualTo("STATUS_DROPDOWN");
    }

    @Test
    void shouldCreateFetchAndDeleteValidationMetadata() {

        TestScreens screens = createDefaultMetadata();
        long customerNameColumnId =
                findColumnId(screens.customerScreenId(), "CUSTOMER_NAME");

        long validationId =
                createValidation(
                        customerNameColumnId,
                        "MAX_LENGTH",
                        "100",
                        "Customer name cannot exceed 100 characters"
                );

        List<Map<String, Object>> validations =
                get("/api/validations/" + customerNameColumnId, List.class);

        assertThat(validations)
                .extracting(validation -> validation.get("validationType"))
                .contains("MAX_LENGTH");

        exchange(HttpMethod.DELETE, "/api/validations/" + validationId, null, Void.class);

        assertThat(get("/api/validations/" + customerNameColumnId, List.class)).isEmpty();
    }

    @Test
    void shouldResolveDropdownOptionsAndRejectBadDropdownRequests() {

        createDefaultMetadata();
        long dropdownId = createDropdown(
                "CUSTOMER_STATUS_BY_ID",
                "Customer Status By ID",
                """
                        SELECT STATUS AS VALUE,
                               STATUS AS LABEL
                        FROM CUSTOMER_MASTER
                        WHERE CUSTOMER_ID = :P_CUSTOMER_ID
                        """
        );
        createDropdownParam(
                dropdownId,
                "P_CUSTOMER_ID",
                "CUSTOMER_ID",
                true,
                1
        );

        List<Map<String, Object>> options =
                get("/api/dropdowns/CUSTOMER_STATUS_BY_ID/options?CUSTOMER_ID=1", List.class);

        assertThat(options).hasSize(1);
        assertThat(options.getFirst().get("value")).isEqualTo("ACTIVE");
        assertThat(options.getFirst().get("label")).isEqualTo("ACTIVE");

        ResponseEntity<Map> missingParam =
                getRaw("/api/dropdowns/CUSTOMER_STATUS_BY_ID/options");
        ResponseEntity<Map> invalidCode =
                getRaw("/api/dropdowns/UNKNOWN_DROPDOWN/options?CUSTOMER_ID=1");

        assertThat(missingParam.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingParam.getBody().get("message").toString())
                .contains("Missing parameter: CUSTOMER_ID");
        assertThat(invalidCode.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(invalidCode.getBody().get("message").toString())
                .contains("Dropdown not found");
    }

    @Test
    void shouldCreateAuditEntriesForInsertUpdateAndDelete() {

        createDefaultMetadata();

        post("/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9400,
                "CUSTOMER_NAME", "Audit Customer",
                "STATUS", "ACTIVE"
        ), Map.class);

        exchange(HttpMethod.PUT, "/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9400,
                "CUSTOMER_NAME", "Audit Customer Updated",
                "STATUS", "INACTIVE"
        ), Map.class);

        exchange(HttpMethod.DELETE, "/api/data/CUSTOMER", Map.of(
                "CUSTOMER_ID", 9400
        ), Map.class);

        List<Map<String, Object>> byScreen =
                get("/api/audit/CUSTOMER", List.class);
        List<Map<String, Object>> byRecord =
                get("/api/audit/CUSTOMER/9400", List.class);

        assertThat(byScreen).isNotEmpty();
        assertThat(byRecord)
                .extracting(audit -> audit.get("operation"))
                .contains("INSERT", "UPDATE", "DELETE");
    }

    private long findColumnId(
            long screenId,
            String columnName) {

        List<Map<String, Object>> columns =
                get("/api/columns/screen/" + screenId, List.class);

        return columns.stream()
                .filter(column -> columnName.equals(column.get("columnName")))
                .findFirst()
                .map(column -> asLong(column.get("id")))
                .orElseThrow();
    }

    private Map<String, Object> findField(
            Map<String, Object> form,
            String columnName) {

        List<Map<String, Object>> fields =
                (List<Map<String, Object>>) form.get("fields");

        return fields.stream()
                .filter(field -> columnName.equals(field.get("columnName")))
                .findFirst()
                .orElseThrow();
    }

    private ResponseEntity<Map> getRaw(String path) {

        return restTemplate.exchange(
                url(path),
                HttpMethod.GET,
                new HttpEntity<>(null, headers()),
                Map.class
        );
    }
}
