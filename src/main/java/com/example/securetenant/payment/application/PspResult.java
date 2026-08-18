package com.example.securetenant.payment.application;

public record PspResult(
        boolean success,
        boolean retryable,
        boolean timedOut,
        String pspReference,
        String errorCode,
        String errorMessage) {

    public static PspResult succeeded(String pspReference) {
        return new PspResult(true, false, false, pspReference, null, null);
    }

    public static PspResult retryable(String errorCode, String errorMessage) {
        return new PspResult(false, true, false, null, errorCode, errorMessage);
    }

    public static PspResult timeout() {
        return new PspResult(false, true, true, null, "TIMEOUT", "PSP did not respond in time");
    }

    public static PspResult failed(String errorCode, String errorMessage) {
        return new PspResult(false, false, false, null, errorCode, errorMessage);
    }
}
