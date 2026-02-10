package com.cantina.biometric.controller;

import com.cantina.biometric.config.BiometricProperties;
import com.cantina.biometric.dto.CandidateRequest;
import com.cantina.biometric.service.BiometricMatchingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BiometricController.class)
class BiometricControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BiometricMatchingService matchingService;

    @Test
    void identifyReturnsExpectedMatch() throws Exception {
        when(matchingService.identify(anyString(), anyList())).thenReturn(
                new BiometricMatchingService.IdentifyResult(true, "uuid-2", 56.2, 56.2, 3)
        );

        String payload = """
                {
                  "scanTemplateBase64": "U0NBTg==",
                  "candidates": [
                    {"candidateId": "uuid-1", "templatesBase64": ["QQ=="]},
                    {"candidateId": "uuid-2", "templatesBase64": ["Qg=="]}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/biometric/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(true))
                .andExpect(jsonPath("$.candidateId").value("uuid-2"))
                .andExpect(jsonPath("$.score").value(56.2));
    }

    @Test
    void identifyReturnsNotFoundWhenBelowThreshold() throws Exception {
        when(matchingService.identify(anyString(), anyList())).thenReturn(
                new BiometricMatchingService.IdentifyResult(false, null, null, 12.3, 2)
        );

        String payload = """
                {
                  "scanTemplateBase64": "U0NBTg==",
                  "candidates": [
                    {"candidateId": "uuid-1", "templatesBase64": ["QQ=="]}
                  ]
                }
                """;

        mockMvc.perform(post("/api/v1/biometric/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matched").value(false))
                .andExpect(jsonPath("$.candidateId").doesNotExist())
                .andExpect(jsonPath("$.maxScore").value(12.3));
    }

    @Test
    void verifyReturnsTrueAndFalse() throws Exception {
        when(matchingService.verify(anyString(), anyList()))
                .thenReturn(new BiometricMatchingService.VerifyResult(true, 49.1, 40.0))
                .thenReturn(new BiometricMatchingService.VerifyResult(false, 10.0, 40.0));

        String payload = """
                {
                  "scanTemplateBase64": "U0NBTg==",
                  "personTemplatesBase64": ["QQ=="]
                }
                """;

        mockMvc.perform(post("/api/v1/biometric/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(true));

        mockMvc.perform(post("/api/v1/biometric/verify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.verified").value(false));
    }

    @Test
    void returnsBadRequestForInvalidPayload() throws Exception {
        String payload = """
                {
                  "scanTemplateBase64": "",
                  "candidates": []
                }
                """;

        mockMvc.perform(post("/api/v1/biometric/identify")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @TestConfiguration
    static class TestConfig {
        @Bean
        BiometricProperties biometricProperties() {
            BiometricProperties props = new BiometricProperties();
            props.setThreshold(40.0);
            props.setMaxCandidates(2000);
            return props;
        }
    }
}
