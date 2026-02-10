package com.cantina.biometric.dto;

public record VerifyResponse(
        boolean verified,
        double score,
        double threshold,
        long elapsedMs
) {
}
