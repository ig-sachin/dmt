package com.dmt.backend.engine.form.dto;

import java.util.List;

public record FormResponse(

        String screenCode,

        String screenName,

        List<FormFieldResponse> fields

) {
}