package com.cantina.biometric.service;

import com.cantina.biometric.config.BiometricProperties;
import com.cantina.biometric.dto.CandidateRequest;
import com.cantina.biometric.exception.BadRequestException;
import com.machinezoo.sourceafis.FingerprintMatcher;
import com.machinezoo.sourceafis.FingerprintTemplate;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

@Service
public class BiometricMatchingService {

    private final BiometricProperties properties;

    public BiometricMatchingService(BiometricProperties properties) {
        this.properties = properties;
    }

    public IdentifyResult identify(String scanTemplateBase64, List<CandidateRequest> candidates) {
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

            if (candidateScore > maxScore) {
                maxScore = candidateScore;
                bestCandidateId = candidate.candidateId();
            }
        }

        boolean matched = maxScore >= properties.getThreshold() && bestCandidateId != null;
        return new IdentifyResult(matched, matched ? bestCandidateId : null, matched ? maxScore : null, maxScore, candidates.size());
    }

    public VerifyResult verify(String scanTemplateBase64, List<String> personTemplatesBase64) {
        FingerprintTemplate scanTemplate = toTemplate(scanTemplateBase64, "scanTemplateBase64");
        FingerprintMatcher matcher = new FingerprintMatcher(scanTemplate);
        double maxScore = 0d;

        for (String personTemplateBase64 : personTemplatesBase64) {
            FingerprintTemplate personTemplate = toTemplate(personTemplateBase64, "personTemplatesBase64");
            double score = matcher.match(personTemplate);
            maxScore = Math.max(maxScore, score);
        }

        return new VerifyResult(maxScore >= properties.getThreshold(), maxScore, properties.getThreshold());
    }

    private FingerprintTemplate toTemplate(String base64, String field) {
        try {
            byte[] templateBytes = Base64.getDecoder().decode(base64);
            return new FingerprintTemplate(templateBytes);
        } catch (IllegalArgumentException ex) {
            throw new BadRequestException("Invalid base64 in field " + field);
        } catch (RuntimeException ex) {
            throw new BadRequestException("Invalid fingerprint template in field " + field);
        }
    }

    public record IdentifyResult(boolean matched, String candidateId, Double score, double maxScore, int candidatesEvaluated) {
    }

    public record VerifyResult(boolean verified, double score, double threshold) {
    }
}
