package com.jeshwin.printelligence.review.infra.ai;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeshwin.printelligence.review.domain.FindingCategory;
import com.jeshwin.printelligence.review.domain.FindingSeverity;
import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import com.jeshwin.printelligence.review.domain.RiskBand;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringAiOpenAiReviewGateway {

    private final ObjectProvider<ChatClient.Builder> chatClientBuilderProvider;
    private final ObjectMapper objectMapper;

    public SpringAiOpenAiReviewGateway(ObjectProvider<ChatClient.Builder> chatClientBuilderProvider,
                                       ObjectMapper objectMapper) {
        this.chatClientBuilderProvider = chatClientBuilderProvider;
        this.objectMapper = objectMapper;
    }

    public ReviewRecommendation tryGenerate(PullRequestSnapshot snapshot,
                                            List<ReviewChunk> chunks,
                                            List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
                                            List<String> architectureContext) {
        ChatClient.Builder builder = chatClientBuilderProvider.getIfAvailable();
        if (builder == null) {
            return null;
        }

        try {
            String raw = builder.build()
                    .prompt()
                    .system(systemPrompt())
                    .user(userPrompt(snapshot, chunks, policyFindings, architectureContext))
                    .call()
                    .content();

            ModelReviewResponse parsed = objectMapper.readValue(stripCodeFences(raw), ModelReviewResponse.class);
            return toRecommendation(parsed, policyFindings);
        } catch (Exception ex) {
            return null;
        }
    }

    private String systemPrompt() {
        return """
                You are an expert Java backend code reviewer.
                Return only valid JSON.
                Do not use markdown fences.
                Preserve the existing findings when they are high-confidence and add architecture-aware recommendations.
                """;
    }

    private String userPrompt(PullRequestSnapshot snapshot,
                              List<ReviewChunk> chunks,
                              List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
                              List<String> architectureContext) throws JsonProcessingException {
        ReviewRequestEnvelope envelope = new ReviewRequestEnvelope(
                snapshot,
                chunks,
                policyFindings,
                architectureContext
        );
        return """
                Review the following pull request and produce a JSON object with this structure:
                {
                  "riskScore": 0-100 integer,
                  "riskBand": "GUARDED|ELEVATED|SEVERE",
                  "executiveSummary": "string",
                  "architectureNotes": "string",
                  "findings": [
                    {
                      "severity": "LOW|MEDIUM|HIGH|CRITICAL",
                      "category": "SECURITY|CODE_SMELL|ARCHITECTURE|PERFORMANCE|MAINTAINABILITY",
                      "filePath": "string",
                      "lineNumber": integer,
                      "title": "string",
                      "description": "string",
                      "recommendation": "string"
                    }
                  ]
                }

                Input:
                """ + objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(envelope);
    }

    private String stripCodeFences(String raw) {
        String trimmed = raw == null ? "" : raw.trim();
        if (trimmed.startsWith("```")) {
            trimmed = trimmed.replaceFirst("^```(?:json)?\\s*", "");
            trimmed = trimmed.replaceFirst("\\s*```$", "");
        }
        return trimmed.trim();
    }

    private ReviewRecommendation toRecommendation(ModelReviewResponse parsed,
                                                  List<ReviewRecommendation.ReviewFindingDraft> policyFindings) {
        List<ReviewRecommendation.ReviewFindingDraft> generatedFindings =
                parsed.findings() == null || parsed.findings().isEmpty()
                        ? policyFindings
                        : parsed.findings().stream()
                        .map(finding -> new ReviewRecommendation.ReviewFindingDraft(
                                FindingSeverity.valueOf(finding.severity()),
                                FindingCategory.valueOf(finding.category()),
                                finding.filePath(),
                                finding.lineNumber(),
                                finding.title(),
                                finding.description(),
                                finding.recommendation()
                        ))
                        .toList();

        return new ReviewRecommendation(
                parsed.riskScore(),
                RiskBand.valueOf(parsed.riskBand()),
                parsed.executiveSummary(),
                parsed.architectureNotes(),
                generatedFindings
        );
    }

    private record ReviewRequestEnvelope(
            PullRequestSnapshot snapshot,
            List<ReviewChunk> chunks,
            List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
            List<String> architectureContext
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelReviewResponse(
            int riskScore,
            String riskBand,
            String executiveSummary,
            String architectureNotes,
            List<ModelFinding> findings
    ) {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private record ModelFinding(
            String severity,
            String category,
            String filePath,
            int lineNumber,
            String title,
            String description,
            String recommendation
    ) {
    }
}
