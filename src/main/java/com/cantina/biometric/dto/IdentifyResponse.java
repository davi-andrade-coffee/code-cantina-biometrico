package com.cantina.biometric.dto;

public record IdentifyResponse(
        boolean matched,
        String candidateId,
        Double score,
        double maxScore,
        int candidatesEvaluated,
        long elapsedMs
) {
}
