package com.dmt.backend;

import org.junit.jupiter.api.Test;

import java.util.Map;

class MetadataIntegrationTest extends AbstractDmtIntegrationTest {

    @Test
    void shouldCreateAndReadMultipleScreensMetadata() {

        TestScreens screens = createDefaultMetadata();

        assertScreenLookup(screens.customerScreenId(), "CUSTOMER");
        assertScreenLookup(screens.employeeScreenId(), "EMPLOYEE");
        assertScreenLookup(screens.orderScreenId(), "ORDER_BOOK");
        assertAllScreensContain("CUSTOMER", "EMPLOYEE", "ORDER_BOOK");

        assertMetadata("CUSTOMER", 3, 2);
        assertMetadata("EMPLOYEE", 4, 2);
        assertMetadata("ORDER_BOOK", 6, 3);

        assertMetadataChildrenCanBeListed(screens.customerScreenId(), 3, 2, 3);
        assertMetadataChildrenCanBeListed(screens.employeeScreenId(), 4, 2, 3);
        assertMetadataChildrenCanBeListed(screens.orderScreenId(), 6, 3, 3);
    }

    @Test
    void shouldDeleteMetadataRecords() {

        createDefaultMetadata();

        assertMetadataDeleteEndpoints();
    }

    @Test
    void shouldCreateScreen() {

        long screenId = createScreen(Map.of(
                "screenCode", "SIMPLE_SCREEN",
                "screenName", "Simple Screen",
                "description", "Simple screen creation test",
                "selectQuery", "SELECT CUSTOMER_ID FROM CUSTOMER_MASTER",
                "defaultPageSize", 10,
                "defaultSortColumn", "CUSTOMER_ID",
                "defaultSortDirection", "ASC",
                "active", true
        ));

        assertScreenLookup(screenId, "SIMPLE_SCREEN");
    }
}
