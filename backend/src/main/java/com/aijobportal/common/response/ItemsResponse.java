package com.aijobportal.common.response;

import java.util.List;

public record ItemsResponse<T>(List<T> items) {
}
