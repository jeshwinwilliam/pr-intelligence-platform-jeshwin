package com.jeshwin.printelligence.review.infra.ai;

import com.jeshwin.printelligence.review.domain.FindingCategory;
import com.jeshwin.printelligence.review.domain.FindingSeverity;
import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import com.jeshwin.printelligence.review.domain.RiskBand;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Component
public class DeterministicAiReviewGateway implements AiReviewGateway {

    @Override
    public ReviewRecommendation generate(PullRequestSnapshot snapshot,
                                         List<ReviewChunk> chunks,
                                         List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
                                         List<String> architectureContext) {
        List<ReviewRecommendation.ReviewFindingDraft> generated = new ArrayList<>(policyFindings);
        generated.add(new ReviewRecommendation.ReviewFindingDraft(
                FindingSeverity.MEDIUM,
                FindingCategory.ARCHITECTURE,
                snapshot.changedFiles().getFirst().path(),
                1,
                "Security and checkout concerns are converging",
                "The PR blends authentication, checkout flow, and runtime trust policy, which increases coupling across boundaries.",
                "Separate auth enforcement, payment orchestration, and environment policy into isolated components with explicit contracts."
        ));

        generated.sort(Comparator.comparing(ReviewRecommendation.ReviewFindingDraft::severity).reversed());

        int criticalWeight = (int) generated.stream().filter(f -> f.severity() == FindingSeverity.CRITICAL).count() * 25;
        int highWeight = (int) generated.stream().filter(f -> f.severity() == FindingSeverity.HIGH).count() * 15;
        int mediumWeight = (int) generated.stream().filter(f -> f.severity() == FindingSeverity.MEDIUM).count() * 8;
        int chunkWeight = chunks.stream().mapToInt(ReviewChunk::estimatedTokens).sum() > 160 ? 8 : 0;
        int riskScore = Math.min(98, 30 + criticalWeight + highWeight + mediumWeight + chunkWeight);
        RiskBand riskBand = riskScore >= 85 ? RiskBand.SEVERE : riskScore >= 60 ? RiskBand.ELEVATED : RiskBand.GUARDED;

        String executiveSummary = "PR Intelligence Platform flagged this pull request as " + riskBand.name().toLowerCase()
                + " risk due to authentication bypass behavior, sensitive request handling, and cross-boundary coupling across " + chunks.size() + " review lanes.";

        String architectureNotes = "Chunk routing: " + chunks.stream()
                .map(chunk -> chunk.chunkId() + "=" + chunk.reviewLane())
                .reduce((left, right) -> left + ", " + right)
                .orElse("none")
                + ". Retrieved guidance: " + String.join(" | ", architectureContext)
                + " Recommended direction: introduce a gateway-verified auth path, move PCI-sensitive processing behind a payment service, and keep controller logic minimal.";

        return new ReviewRecommendation(riskScore, riskBand, executiveSummary, architectureNotes, generated);
    }
}
