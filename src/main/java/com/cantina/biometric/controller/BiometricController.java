package com.cantina.biometric.controller;

import com.cantina.biometric.config.BiometricProperties;
import com.cantina.biometric.config.RequestIdFilter;
import com.cantina.biometric.dto.IdentifyRequest;
import com.cantina.biometric.dto.IdentifyResponse;
import com.cantina.biometric.dto.VerifyRequest;
import com.cantina.biometric.dto.VerifyResponse;
import com.cantina.biometric.service.BiometricMatchingService;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/biometric")
public class BiometricController {

    private static final Logger log = LoggerFactory.getLogger(BiometricController.class);

    private final BiometricMatchingService matchingService;
    private final BiometricProperties properties;

    public BiometricController(BiometricMatchingService matchingService, BiometricProperties properties) {
        this.matchingService = matchingService;
        this.properties = properties;
    }

    @PostMapping("/identify")
    public IdentifyResponse identify(@Valid @RequestBody IdentifyRequest request) {
        log.info("event=identify-request-received requestId={} candidatesCount={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.candidates().size());

        long start = System.nanoTime();
        var result = matchingService.identify(request.scanTemplateBase64(), request.candidates());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        log.info("event=identify requestId={} candidatesEvaluated={} elapsedMs={} maxScore={} matched={} threshold={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                result.candidatesEvaluated(),
                elapsedMs,
                result.maxScore(),
                result.matched(),
                properties.getThreshold());

        return new IdentifyResponse(
                result.matched(),
                result.candidateId(),
                result.score(),
                result.maxScore(),
                result.candidatesEvaluated(),
                elapsedMs
        );
    }

    @PostMapping("/verify")
    public VerifyResponse verify(@Valid @RequestBody VerifyRequest request) {
        log.info("event=verify-request-received requestId={} templatesCount={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.personTemplatesBase64().size());

        long start = System.nanoTime();
        var result = matchingService.verify(request.scanTemplateBase64(), request.personTemplatesBase64());
        long elapsedMs = (System.nanoTime() - start) / 1_000_000;

        log.info("event=verify requestId={} templatesEvaluated={} elapsedMs={} maxScore={} matched={} threshold={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                request.personTemplatesBase64().size(),
                elapsedMs,
                result.score(),
                result.verified(),
                result.threshold());

        return new VerifyResponse(result.verified(), result.score(), result.threshold(), elapsedMs);
    }
}
