INSERT INTO DMT_SCREEN
(
    ID,
    SCREEN_NAME,
    SCREEN_CODE,
    DESCRIPTION,
    ACTIVE,
    DEFAULT_SORT_COLUMN,
    DEFAULT_SORT_DIRECTION,
    DEFAULT_PAGE_SIZE,
    SELECT_QUERY
)
VALUES
    (
        1,
        'Customer Management',
        'CUSTOMER',
        'Customer Screen',
        1,
        'CUSTOMER_ID',
        'ASC',
        20,
        'SELECT CUSTOMER_ID, CUSTOMER_NAME, STATUS FROM CUSTOMER_MASTER'
    );

INSERT INTO DMT_COLUMN
(
    ID,
    SCREEN_ID,
    COLUMN_NAME,
    DISPLAY_NAME,
    DATA_TYPE,
    FIELD_TYPE,
    VISIBLE,
    EDITABLE,
    MANDATORY,
    DISPLAY_ORDER,
    WIDTH
)
VALUES
    (
        1,
        1,
        'CUSTOMER_ID',
        'Customer ID',
        'NUMBER',
        'TEXT',
        1,
        0,
        0,
        1,
        100
    );

INSERT INTO DMT_COLUMN
(
    ID,
    SCREEN_ID,
    COLUMN_NAME,
    DISPLAY_NAME,
    DATA_TYPE,
    FIELD_TYPE,
    VISIBLE,
    EDITABLE,
    MANDATORY,
    DISPLAY_ORDER,
    WIDTH
)
VALUES
    (
        2,
        1,
        'CUSTOMER_NAME',
        'Customer Name',
        'VARCHAR2',
        'TEXT',
        1,
        1,
        1,
        2,
        200
    );

INSERT INTO DMT_COLUMN
(
    ID,
    SCREEN_ID,
    COLUMN_NAME,
    DISPLAY_NAME,
    DATA_TYPE,
    FIELD_TYPE,
    VISIBLE,
    EDITABLE,
    MANDATORY,
    DISPLAY_ORDER,
    WIDTH
)
VALUES
    (
        3,
        1,
        'STATUS',
        'Status',
        'VARCHAR2',
        'DROPDOWN',
        1,
        1,
        1,
        3,
        150
    );

INSERT INTO DMT_FILTER
(
    ID,
    SCREEN_ID,
    FILTER_NAME,
    COLUMN_NAME,
    FILTER_TYPE,
    REQUIRED,
    DISPLAY_ORDER
)
VALUES
    (
        1,
        1,
        'Customer Name',
        'CUSTOMER_NAME',
        'TEXT',
        0,
        1
    );

INSERT INTO DMT_FILTER
(
    ID,
    SCREEN_ID,
    FILTER_NAME,
    COLUMN_NAME,
    FILTER_TYPE,
    REQUIRED,
    DISPLAY_ORDER
)
VALUES
    (
        2,
        1,
        'Status',
        'STATUS',
        'MULTI_SELECT',
        0,
        2
    );

INSERT INTO DMT_PROCEDURE
(
    ID,
    SCREEN_ID,
    OPERATION_TYPE,
    PROCEDURE_NAME,
    ACTIVE
)
VALUES
    (
        1,
        1,
        'INSERT',
        'PKG_CUSTOMER.INSERT_CUSTOMER',
        1
    );

INSERT INTO DMT_PROCEDURE
(
    ID,
    SCREEN_ID,
    OPERATION_TYPE,
    PROCEDURE_NAME,
    ACTIVE
)
VALUES
    (
        2,
        1,
        'UPDATE',
        'PKG_CUSTOMER.UPDATE_CUSTOMER',
        1
    );

INSERT INTO DMT_PROCEDURE
(
    ID,
    SCREEN_ID,
    OPERATION_TYPE,
    PROCEDURE_NAME,
    ACTIVE
)
VALUES
    (
        3,
        1,
        'DELETE',
        'PKG_CUSTOMER.DELETE_CUSTOMER',
        1
    );

INSERT INTO DMT_PROCEDURE_PARAM
(
    ID,
    PROCEDURE_ID,
    PARAMETER_NAME,
    PARAMETER_ORDER,
    COLUMN_NAME,
    REQUIRED
)
VALUES
    (
        1,
        2,
        'P_CUSTOMER_ID',
        1,
        'CUSTOMER_ID',
        1
    );

INSERT INTO DMT_PROCEDURE_PARAM
(
    ID,
    PROCEDURE_ID,
    PARAMETER_NAME,
    PARAMETER_ORDER,
    COLUMN_NAME,
    REQUIRED
)
VALUES
    (
        2,
        2,
        'P_CUSTOMER_NAME',
        2,
        'CUSTOMER_NAME',
        1
    );

INSERT INTO DMT_PROCEDURE_PARAM
(
    ID,
    PROCEDURE_ID,
    PARAMETER_NAME,
    PARAMETER_ORDER,
    COLUMN_NAME,
    REQUIRED
)
VALUES
    (
        3,
        2,
        'P_STATUS',
        3,
        'STATUS',
        1
    );

INSERT INTO DMT_PROCEDURE_PARAM
(
    ID,
    PROCEDURE_ID,
    PARAMETER_NAME,
    PARAMETER_ORDER,
    COLUMN_NAME,
    REQUIRED
)
VALUES
    (
        4,
        3,
        'P_CUSTOMER_ID',
        1,
        'CUSTOMER_ID',
        1
    );