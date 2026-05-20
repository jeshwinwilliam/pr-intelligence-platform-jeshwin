package com.jeshwin.printelligence.review.infra.policy;

import com.jeshwin.printelligence.review.domain.PolicyPack;
import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;

import java.util.List;

public interface PolicyRuleEngine {
    List<ReviewRecommendation.ReviewFindingDraft> analyze(PullRequestSnapshot snapshot, PolicyPack policyPack);
}
