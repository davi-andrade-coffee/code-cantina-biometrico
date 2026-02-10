package com.cantina.biometric.service;

import com.cantina.biometric.config.BiometricProperties;
import com.cantina.biometric.dto.CandidateRequest;
import com.cantina.biometric.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;

class BiometricMatchingServiceTest {

    @Test
    void identifyRejectsInvalidBase64() {
        BiometricProperties properties = new BiometricProperties();
        BiometricMatchingService service = new BiometricMatchingService(properties);

        assertThrows(BadRequestException.class,
                () -> service.identify("%%%", List.of(new CandidateRequest("id-1", List.of("AAA=")))));
    }

    @Test
    void verifyRejectsInvalidBase64() {
        BiometricProperties properties = new BiometricProperties();
        BiometricMatchingService service = new BiometricMatchingService(properties);

        assertThrows(BadRequestException.class,
                () -> service.verify("AAA=", List.of("%%%")));
    }
}
