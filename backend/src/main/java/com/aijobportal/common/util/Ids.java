package com.aijobportal.common.util;

import java.util.UUID;

public final class Ids {

    private Ids() {
    }

    public static String next() {
        return UUID.randomUUID().toString();
    }
}
