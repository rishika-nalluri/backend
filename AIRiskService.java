package com.pawconnect.health;

import com.pawconnect.ai.AiGatewayClient;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Screens symptoms with the AI service and falls back to local rules when it is unavailable. The fallback
 * is labelled as such in the stored record, so an outage never looks like a model result.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AIRiskService {

    private final AIRiskAssessmentRepository aiRiskAssessmentRepository;
    private final AiGatewayClient gateway;

    public AIRiskAssessment assessRisk(DogHealthRiskInput input) {
        String inputHash = computeHash(input);
        Optional<AIRiskAssessment> cached = aiRiskAssessmentRepository.findFirstByInputHash(inputHash);
        if (cached.isPresent()) {
            return cached.get();
        }

        AIRiskResponse response = screen(input);
        AIRiskAssessment assessment = AIRiskAssessment.builder()
                .dogId(input.getDogId())
                .sourceSymptomLogId(input.getSymptomLogId())
                .riskLevel(response.getRiskLevel())
                .confidenceScore(BigDecimal.valueOf(response.getConfidenceScore()))
                .explanation(response.getExplanation())
                .recommendations(response.getRecommendations())
                .modelProvider(response.getModelProvider())
                .modelVersion(response.getModelVersion())
                .inputHash(inputHash)
                .build();
        return aiRiskAssessmentRepository.save(assessment);
    }

    /** Screens without saving. Used by the chat-side symptom preview. */
    public AIRiskResponse preview(DogHealthRiskInput input) {
        return screen(input);
    }

    private AIRiskResponse screen(DogHealthRiskInput input) {
        try {
            Map<String, Object> reply = gateway.risk(toModelPayload(input));
            AIRiskResponse fromModel = fromModel(reply);
            if (fromModel != null) {
                return fromModel;
            }
        } catch (RuntimeException e) {
            log.warn("AI service unavailable, using rule-based fallback: {}", e.getMessage());
        }
        return fallbackDeterministicScreening(input);
    }

    /** The AI service's request contract: snake_case, with the enum values it accepts. */
    static Map<String, Object> toModelPayload(DogHealthRiskInput in) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("appetite", appetite(in.getAppetite()));
        m.put("stool_consistency", stool(in.getStoolConsistency()));
        m.put("vomiting_episodes", Boolean.TRUE.equals(in.getVomiting()) ? 1 : 0);
        m.put("lethargy", Boolean.TRUE.equals(in.getLethargy()) ? "MILD" : "NORMAL");
        m.put("water_intake", water(in.getWaterIntake()));
        return m;
    }

    static String appetite(String raw) {
        String v = upper(raw);
        return Set.of("NORMAL", "REDUCED", "NONE", "INCREASED").contains(v) ? v : "NORMAL";
    }

    static String stool(String raw) {
        return switch (upper(raw)) {
            case "WATERY", "DIARRHOEA" -> "DIARRHOEA";
            case "BLOODY", "BLOOD_TINGED" -> "BLOOD_TINGED";
            case "SOFT" -> "SOFT";
            case "HARD" -> "HARD";
            default -> "NORMAL";
        };
    }

    static String water(String raw) {
        return switch (upper(raw)) {
            case "INCREASED" -> "INCREASED";
            case "DECREASED", "REDUCED" -> "REDUCED";
            default -> "NORMAL";
        };
    }

    private static String upper(String raw) {
        return raw == null ? "" : raw.trim().toUpperCase(Locale.ROOT);
    }

    private static AIRiskResponse fromModel(Map<String, Object> reply) {
        if (reply == null || !(reply.get("risk_level") instanceof String level)) {
            return null;
        }
        AIRiskResponse r = new AIRiskResponse();
        r.setRiskLevel(level);
        r.setConfidenceScore(reply.get("confidence") instanceof Number n ? n.doubleValue() : 0.5);
        r.setExplanation(reply.get("explanation") instanceof String s ? s : "");
        List<String> reasons = new ArrayList<>();
        if (reply.get("reasons") instanceof List<?> list) {
            list.forEach(item -> reasons.add(String.valueOf(item)));
        }
        r.setRecommendations(String.join("\n", reasons));
        r.setModelProvider(reply.get("model_provider") instanceof String p ? p : "ai-service");
        r.setModelVersion(reply.get("model_version") instanceof String v ? v : "unknown");
        return r;
    }

    private String computeHash(DogHealthRiskInput input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            String raw = String.format("%s|%s|%s|%b|%b|%s",
                    input.getDogId(), input.getAppetite(), input.getStoolConsistency(),
                    input.getVomiting(), input.getLethargy(), input.getWaterIntake());
            return HexFormat.of().formatHex(md.digest(raw.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            return UUID.randomUUID().toString();
        }
    }

    private AIRiskResponse fallbackDeterministicScreening(DogHealthRiskInput input) {
        AIRiskResponse resp = new AIRiskResponse();
        resp.setModelProvider("rule-engine-fallback");
        resp.setModelVersion("1.0.0");

        boolean severeStool = "WATERY".equalsIgnoreCase(input.getStoolConsistency())
                || "BLOODY".equalsIgnoreCase(input.getStoolConsistency());
        boolean severeAppetite = "NONE".equalsIgnoreCase(input.getAppetite());
        boolean vomiting = Boolean.TRUE.equals(input.getVomiting());
        boolean lethargy = Boolean.TRUE.equals(input.getLethargy());

        if (severeStool || (vomiting && lethargy) || (severeAppetite && vomiting)) {
            resp.setRiskLevel("CONSULT_VET");
            resp.setConfidenceScore(0.92);
            resp.setExplanation("Multiple high-priority symptoms detected. Veterinary attention is advised.");
            resp.setRecommendations("1. Book a veterinary consultation today.\n2. Do not offer heavy food until evaluated.\n3. Offer small sips of clean water.");
        } else if (vomiting || lethargy || "SOFT".equalsIgnoreCase(input.getStoolConsistency())
                || "REDUCED".equalsIgnoreCase(input.getAppetite())) {
            resp.setRiskLevel("MODERATE");
            resp.setConfidenceScore(0.80);
            resp.setExplanation("Mild to moderate digestive irritation or fatigue observed. Monitor closely for 12 to 24 hours.");
            resp.setRecommendations("1. Monitor hydration and stool consistency.\n2. Offer a bland diet if tolerated.\n3. Consult a vet if symptoms persist beyond 24 hours.");
        } else {
            resp.setRiskLevel("LOW");
            resp.setConfidenceScore(0.95);
            resp.setExplanation("Reported signs appear within baseline parameters with no acute red flags.");
            resp.setRecommendations("1. Continue regular feeding and exercise.\n2. Keep up hydration and scheduled vaccinations.");
        }
        return resp;
    }

    @Data
    public static class DogHealthRiskInput {
        private UUID dogId;
        private UUID symptomLogId;
        private String symptomsText;
        private String appetite;
        private String stoolConsistency;
        private Boolean vomiting;
        private Boolean lethargy;
        private String waterIntake;
    }

    @Data
    public static class AIRiskResponse {
        private String riskLevel;
        private double confidenceScore;
        private String explanation;
        private String recommendations;
        private String modelProvider;
        private String modelVersion;
    }
}
