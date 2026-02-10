package com.cantina.biometric.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record VerifyRequest(
        @NotBlank(message = "scanTemplateBase64 is required")
        String scanTemplateBase64,
        @NotEmpty(message = "personTemplatesBase64 must contain at least one template")
        List<@NotBlank(message = "template in personTemplatesBase64 cannot be blank") String> personTemplatesBase64
) {
}
