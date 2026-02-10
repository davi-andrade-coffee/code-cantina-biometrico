package com.cantina.biometric.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record IdentifyRequest(
        @NotBlank(message = "scanTemplateBase64 is required")
        String scanTemplateBase64,
        @NotEmpty(message = "candidates must contain at least one candidate")
        List<@Valid CandidateRequest> candidates
) {
}
