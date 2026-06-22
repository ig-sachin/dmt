package com.dmt.backend;

import com.dmt.backend.metadata.column.dto.ColumnResponse;
import com.dmt.backend.metadata.column.entity.FieldType;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ColumnResponseTest {

    @Test
    void shouldCreateColumnResponseWithAllFields() {
        ColumnResponse response = new ColumnResponse(
                1L, 100L, "CUSTOMER_ID", "Customer ID", "NUMBER", FieldType.NUMBER,
                true, false, true, null, 1, 120, "LEFT", null, null, 10, null
        );

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.screenId()).isEqualTo(100L);
        assertThat(response.columnName()).isEqualTo("CUSTOMER_ID");
        assertThat(response.displayName()).isEqualTo("Customer ID");
        assertThat(response.visible()).isTrue();
        assertThat(response.editable()).isFalse();
        assertThat(response.mandatory()).isTrue();
    }

    @Test
    void shouldHandleNullOptionalFields() {
        ColumnResponse response = new ColumnResponse(
                2L, 100L, "NAME", "Customer Name", "VARCHAR", FieldType.TEXT,
                true, true, false, null, 2, 200, "LEFT", null, null, 50, null
        );

        assertThat(response.defaultValue()).isNull();
        assertThat(response.formatMask()).isNull();
        assertThat(response.placeHolder()).isNull();
        assertThat(response.dropdownCode()).isNull();
    }

    @Test
    void shouldPreserveFieldTypeEnum() {
        ColumnResponse response = new ColumnResponse(
                3L, 100L, "STATUS", "Status", "VARCHAR", FieldType.DROPDOWN,
                true, true, true, "ACTIVE", 3, 100, "CENTER", null, null, 20, "STATUS_DROPDOWN"
        );

        assertThat(response.fieldType()).isEqualTo(FieldType.DROPDOWN);
        assertThat(response.dropdownCode()).isEqualTo("STATUS_DROPDOWN");
    }

    @Test
    void shouldHandleAlignmentValues() {
        String[] alignments = {"LEFT", "CENTER", "RIGHT"};

        for (String alignment : alignments) {
            ColumnResponse response = new ColumnResponse(
                    4L, 100L, "COL", "Column", "VARCHAR", FieldType.TEXT,
                    true, false, false, null, 1, 100, alignment, null, null, 10, null
            );

            assertThat(response.alignment()).isEqualTo(alignment);
        }
    }

    @Test
    void shouldHandleNumericFields() {
        ColumnResponse response = new ColumnResponse(
                5L, 100L, "AMOUNT", "Amount", "NUMBER", FieldType.NUMBER,
                true, true, true, "0", 5, 120, "RIGHT", null, null, 15, null
        );

        assertThat(response.displayOrder()).isEqualTo(5);
        assertThat(response.width()).isEqualTo(120);
        assertThat(response.maxLength()).isEqualTo(15);
        assertThat(response.defaultValue()).isEqualTo("0");
    }

    @Test
    void shouldHandleDateTypeColumn() {
        ColumnResponse response = new ColumnResponse(
                6L, 100L, "CREATED_DATE", "Created Date", "DATE", FieldType.DATE,
                true, false, true, null, 6, 150, "CENTER", "MM/DD/YYYY", "Select date", 10, null
        );

        assertThat(response.dataType()).isEqualTo("DATE");
        assertThat(response.fieldType()).isEqualTo(FieldType.DATE);
        assertThat(response.formatMask()).isEqualTo("MM/DD/YYYY");
        assertThat(response.placeHolder()).isEqualTo("Select date");
    }

    @Test
    void shouldHandleTextAreaField() {
        ColumnResponse response = new ColumnResponse(
                7L, 100L, "DESCRIPTION", "Description", "VARCHAR", FieldType.TEXTAREA,
                true, true, false, null, 7, 300, "LEFT", null, "Enter description...", 500, null
        );

        assertThat(response.fieldType()).isEqualTo(FieldType.TEXTAREA);
        assertThat(response.width()).isEqualTo(300);
        assertThat(response.maxLength()).isEqualTo(500);
    }

    @Test
    void shouldHandleEditableAndVisibleFlags() {
        ColumnResponse editableVisible = new ColumnResponse(
                8L, 100L, "NAME", "Name", "VARCHAR", FieldType.TEXT,
                true, true, true, null, 1, 200, "LEFT", null, null, 100, null
        );

        ColumnResponse readOnlyHidden = new ColumnResponse(
                9L, 100L, "ID", "ID", "NUMBER", FieldType.NUMBER,
                false, false, true, null, 1, 100, "LEFT", null, null, 10, null
        );

        assertThat(editableVisible.visible()).isTrue();
        assertThat(editableVisible.editable()).isTrue();

        assertThat(readOnlyHidden.visible()).isFalse();
        assertThat(readOnlyHidden.editable()).isFalse();
    }

    @Test
    void shouldHandleCheckboxField() {
        ColumnResponse response = new ColumnResponse(
                10L, 100L, "IS_ACTIVE", "Is Active", "BOOLEAN", FieldType.BOOLEAN,
                true, true, false, "N", 8, 80, "CENTER", null, null, 1, null
        );

        assertThat(response.fieldType()).isEqualTo(FieldType.BOOLEAN);
        assertThat(response.defaultValue()).isEqualTo("N");
    }

    @Test
    void shouldHandleHiddenField() {
        ColumnResponse response = new ColumnResponse(
                11L, 100L, "INTERNAL_ID", "Internal ID", "VARCHAR", FieldType.TEXT,
                false, false, false, null, 99, 0, "LEFT", null, null, 50, null
        );

        assertThat(response.fieldType()).isEqualTo(FieldType.TEXT);
        assertThat(response.visible()).isFalse();
        assertThat(response.editable()).isFalse();
    }

    @Test
    void shouldHandleMultipleDropdownsInSameScreen() {
        ColumnResponse status = new ColumnResponse(
                12L, 100L, "STATUS", "Status", "VARCHAR", FieldType.DROPDOWN,
                true, true, true, "ACTIVE", 1, 100, "LEFT", null, null, 20, "STATUS_LIST"
        );

        ColumnResponse category = new ColumnResponse(
                13L, 100L, "CATEGORY", "Category", "VARCHAR", FieldType.DROPDOWN,
                true, true, false, null, 2, 150, "LEFT", null, null, 30, "CATEGORY_LIST"
        );

        assertThat(status.dropdownCode()).isEqualTo("STATUS_LIST");
        assertThat(category.dropdownCode()).isEqualTo("CATEGORY_LIST");
        assertThat(status.dropdownCode()).isNotEqualTo(category.dropdownCode());
    }

    @Test
    void shouldHandleMandatoryFieldValidation() {
        ColumnResponse mandatory = new ColumnResponse(
                14L, 100L, "EMAIL", "Email", "VARCHAR", FieldType.EMAIL,
                true, true, true, null, 3, 200, "LEFT", null, "user@example.com", 100, null
        );

        ColumnResponse optional = new ColumnResponse(
                15L, 100L, "PHONE", "Phone", "VARCHAR", FieldType.TEXT,
                true, true, false, null, 4, 150, "LEFT", null, "+1-XXX-XXX-XXXX", 15, null
        );

        assertThat(mandatory.mandatory()).isTrue();
        assertThat(optional.mandatory()).isFalse();
    }

    @Test
    void shouldCreateResponseWithMaxWidthAndLength() {
        ColumnResponse response = new ColumnResponse(
                16L, 100L, "FULL_TEXT", "Full Text", "VARCHAR", FieldType.TEXTAREA,
                true, true, false, null, 10, 500, "LEFT", null, "Enter large text", 5000, null
        );

        assertThat(response.width()).isEqualTo(500);
        assertThat(response.maxLength()).isEqualTo(5000);
    }

    @Test
    void shouldHandleScreenIdAssociation() {
        long screenId = 999L;
        ColumnResponse response = new ColumnResponse(
                17L, screenId, "TEST", "Test", "VARCHAR", FieldType.TEXT,
                true, true, true, null, 1, 100, "LEFT", null, null, 50, null
        );

        assertThat(response.screenId()).isEqualTo(screenId);
    }
}
