package com.dmt.backend;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class ScreenRoleControllerIntegrationTest extends AbstractDmtIntegrationTest {
    @Test
    void shouldCreateListAndDeleteScreenRoleMapping() {

        long screenId = createScreen(Map.of(
                "screenCode", "ROLE_TEST",
                "screenName", "Role Test",
                "description", "Screen role controller test",
                "selectQuery", "SELECT CUSTOMER_ID FROM CUSTOMER_MASTER",
                "defaultPageSize", 10,
                "defaultSortColumn", "CUSTOMER_ID",
                "defaultSortDirection", "ASC",
                "primaryKeyColumn", "CUSTOMER_ID",
                "active", true
        ));

        long roleId = createScreenRole(screenId, "ROLE_USER");

        List<Map<String, Object>> roles =
                get("/api/screen-roles/ROLE_TEST", List.class);

        assertThat(roles)
                .extracting(role -> role.get("roleName"))
                .contains("ROLE_USER");

        exchange(HttpMethod.DELETE, "/api/screen-roles/" + roleId, null, Void.class);

        assertThat(get("/api/screen-roles/ROLE_TEST", List.class)).isEmpty();
    }
}
