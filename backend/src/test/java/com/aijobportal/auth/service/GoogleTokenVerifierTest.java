package com.aijobportal.auth.service;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GoogleTokenVerifierTest {

    @Test
    void isEmailVerifiedAcceptsBooleanTrue() {
        assertTrue(GoogleTokenVerifier.isEmailVerified(true));
    }

    @Test
    void isEmailVerifiedAcceptsStringTrue() {
        assertTrue(GoogleTokenVerifier.isEmailVerified("true"));
    }

    @Test
    void isEmailVerifiedRejectsFalseValues() {
        assertFalse(GoogleTokenVerifier.isEmailVerified(false));
        assertFalse(GoogleTokenVerifier.isEmailVerified("false"));
        assertFalse(GoogleTokenVerifier.isEmailVerified(null));
    }
}
