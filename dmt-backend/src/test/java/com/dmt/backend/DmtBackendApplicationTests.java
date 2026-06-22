package com.dmt.backend;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({
        CrudIntegrationTest.class,
        MetadataEngineIntegrationTest.class,
        MetadataIntegrationTest.class,
        ProcedureEngineIntegrationTest.class,
        QueryEngineIntegrationTest.class,
        SecurityIntegrationTest.class,
        ScreenAuthorizationServiceTest.class,
        ScreenRoleControllerIntegrationTest.class,
        ColumnResponseTest.class
})
public class DmtBackendApplicationTests {
}
