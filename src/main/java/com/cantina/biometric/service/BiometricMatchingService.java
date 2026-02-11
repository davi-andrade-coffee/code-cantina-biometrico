package com.cantina.biometric.service;

import com.cantina.biometric.config.BiometricProperties;
import com.cantina.biometric.config.RequestIdFilter;
import com.cantina.biometric.dto.CandidateRequest;
import com.cantina.biometric.exception.BadRequestException;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class BiometricMatchingService {

    private static final Logger log = LoggerFactory.getLogger(BiometricMatchingService.class);

    private final BiometricProperties properties;

    public BiometricMatchingService(BiometricProperties properties) {
        this.properties = properties;
    }

    public IdentifyResult identify(String scanTemplateBase64, List<CandidateRequest> candidates) {
        log.info("event=identify-start requestId={} candidatesCount={} threshold={} maxCandidates={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                candidates.size(),
                properties.getThreshold(),
                properties.getMaxCandidates());

        if (candidates.size() > properties.getMaxCandidates()) {
            throw new BadRequestException("candidates exceeds configured MAX_CANDIDATES=" + properties.getMaxCandidates());
        }

        FingerprintTemplate scanTemplate = toTemplate(scanTemplateBase64, "scanTemplateBase64");
        FingerprintMatcher matcher = new FingerprintMatcher(scanTemplate);

        String bestCandidateId = null;
        double maxScore = 0d;

        for (CandidateRequest candidate : candidates) {
            double candidateScore = 0d;
            for (String candidateTemplateBase64 : candidate.templatesBase64()) {
                FingerprintTemplate candidateTemplate = toTemplate(candidateTemplateBase64,
                        "templatesBase64 for candidateId=" + candidate.candidateId());
                double score = matcher.match(candidateTemplate);
                candidateScore = Math.max(candidateScore, score);
            }

            log.debug("event=identify-candidate-evaluated requestId={} candidateId={} templatesCount={} candidateMaxScore={}",
                    MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                    candidate.candidateId(),
                    candidate.templatesBase64().size(),
                    candidateScore);

            if (candidateScore > maxScore) {
                maxScore = candidateScore;
                bestCandidateId = candidate.candidateId();
            }
        }

        boolean matched = maxScore >= properties.getThreshold() && bestCandidateId != null;
        log.info("event=identify-finish requestId={} matched={} bestCandidateId={} maxScore={} candidatesEvaluated={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                matched,
                matched ? bestCandidateId : null,
                maxScore,
                candidates.size());
        return new IdentifyResult(matched, matched ? bestCandidateId : null, matched ? maxScore : null, maxScore, candidates.size());
    }

    public VerifyResult verify(String scanTemplateBase64, List<String> personTemplatesBase64) {
        log.info("event=verify-start requestId={} templatesCount={} threshold={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                personTemplatesBase64.size(),
                properties.getThreshold());

        FingerprintTemplate scanTemplate = toTemplate(scanTemplateBase64, "scanTemplateBase64");
        FingerprintMatcher matcher = new FingerprintMatcher(scanTemplate);
        double maxScore = 0d;

        for (String personTemplateBase64 : personTemplatesBase64) {
            FingerprintTemplate personTemplate = toTemplate(personTemplateBase64, "personTemplatesBase64");
            double score = matcher.match(personTemplate);
            maxScore = Math.max(maxScore, score);
        }

        log.info("event=verify-finish requestId={} verified={} maxScore={} templatesEvaluated={}",
                MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                maxScore >= properties.getThreshold(),
                maxScore,
                personTemplatesBase64.size());

        return new VerifyResult(maxScore >= properties.getThreshold(), maxScore, properties.getThreshold());
    }

    private FingerprintTemplate toTemplate(String base64, String field) {
        try {
            byte[] templateBytes = Base64.getDecoder().decode(base64);
            return new FingerprintTemplate(templateBytes);
        } catch (IllegalArgumentException ex) {
            log.warn("event=template-decode-error requestId={} field={} reason=invalid-base64 message={}",
                    MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                    field,
                    ex.getMessage());
            throw new BadRequestException("Invalid base64 in field " + field);
        } catch (RuntimeException ex) {
            log.warn("event=template-decode-error requestId={} field={} reason=invalid-template message={}",
                    MDC.get(RequestIdFilter.REQUEST_ID_MDC_KEY),
                    field,
                    ex.getMessage());
            throw new BadRequestException("Invalid fingerprint template in field " + field);
        }
    }

    public record IdentifyResult(boolean matched, String candidateId, Double score, double maxScore, int candidatesEvaluated) {
    }

    public record VerifyResult(boolean verified, double score, double threshold) {
    }
}
