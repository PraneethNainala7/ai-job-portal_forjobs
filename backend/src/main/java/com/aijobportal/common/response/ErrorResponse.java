package com.aijobportal.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ErrorResponse(String error, String code) {

    public ErrorResponse(String error) {
        this(error, null);
    }
}
