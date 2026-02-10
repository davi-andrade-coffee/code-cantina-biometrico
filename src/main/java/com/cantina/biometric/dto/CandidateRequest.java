package com.cantina.biometric.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record CandidateRequest(
        @NotBlank(message = "candidateId is required")
        String candidateId,
        @NotEmpty(message = "templatesBase64 must contain at least one template")
        List<@NotBlank(message = "template in templatesBase64 cannot be blank") String> templatesBase64
) {
}
