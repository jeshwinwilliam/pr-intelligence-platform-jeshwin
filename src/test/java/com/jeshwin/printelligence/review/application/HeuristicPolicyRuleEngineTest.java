package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.FindingSeverity;
import com.jeshwin.printelligence.review.domain.PolicyPack;
import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import com.jeshwin.printelligence.review.infra.policy.HeuristicPolicyRuleEngine;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeuristicPolicyRuleEngineTest {

    private final HeuristicPolicyRuleEngine engine = new HeuristicPolicyRuleEngine();

    @Test
    void shouldDetectSecurityAndMaintainabilityFindingsFromPatch() {
        PullRequestSnapshot snapshot = new PullRequestSnapshot(
                "acme/payments",
                41,
                "Change auth and checkout",
                "jwill",
                List.of(
                        new PullRequestSnapshot.ChangedFile(
                                "AuthFilter.java",
                                "java",
                                52,
                                4,
                                """
                                + if (request.getHeader("X-Internal-Auth") != null) { return true; }
                                + logger.info("request={}", request);
                                + paymentClient.charge(request.cardNumber(), request.amount());
                                """
                        )
                )
        );

        List<ReviewRecommendation.ReviewFindingDraft> findings = engine.analyze(snapshot, PolicyPack.FINTECH);

        assertThat(findings).hasSizeGreaterThanOrEqualTo(3);
        assertThat(findings).anyMatch(finding -> finding.severity() == FindingSeverity.CRITICAL);
        assertThat(findings).anyMatch(finding -> finding.title().contains("Sensitive request logging"));
    }
}
