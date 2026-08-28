package com.aijobportal.auth.dto;

public record OkResponse(boolean ok) {
    public static OkResponse yes() {
        return new OkResponse(true);
    }
}
