package com.jeshwin.printelligence.review.infra.ai;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Primary
public class HybridAiReviewGateway implements AiReviewGateway {

    private final String provider;
    private final SpringAiOpenAiReviewGateway springAiOpenAiReviewGateway;
    private final DeterministicFallbackReviewSynthesizer fallbackSynthesizer;

    public HybridAiReviewGateway(@Value("${pr.intelligence.ai.provider:deterministic}") String provider,
                                 SpringAiOpenAiReviewGateway springAiOpenAiReviewGateway,
                                 DeterministicFallbackReviewSynthesizer fallbackSynthesizer) {
        this.provider = provider;
        this.springAiOpenAiReviewGateway = springAiOpenAiReviewGateway;
        this.fallbackSynthesizer = fallbackSynthesizer;
    }

    @Override
    public ReviewRecommendation generate(PullRequestSnapshot snapshot,
                                         List<ReviewChunk> chunks,
                                         List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
                                         List<String> architectureContext) {
        if ("openai".equalsIgnoreCase(provider)) {
            ReviewRecommendation generated = springAiOpenAiReviewGateway.tryGenerate(
                    snapshot,
                    chunks,
                    policyFindings,
                    architectureContext
            );
            if (generated != null) {
                return generated;
            }
        }

        return fallbackSynthesizer.generate(snapshot, chunks, policyFindings, architectureContext);
    }
}
