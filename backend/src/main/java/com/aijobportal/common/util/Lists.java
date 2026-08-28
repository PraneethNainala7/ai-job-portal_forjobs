package com.aijobportal.common.util;

import java.util.ArrayList;
import java.util.List;

public final class Lists {

    private Lists() {
    }

    public static List<String> copy(List<String> values) {
        return values == null ? new ArrayList<>() : new ArrayList<>(values);
    }
}
