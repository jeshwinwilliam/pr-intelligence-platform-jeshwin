package com.jeshwin.printelligence.review.infra.ai;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;

import java.util.List;

public interface AiReviewGateway {
    ReviewRecommendation generate(PullRequestSnapshot snapshot,
                                  List<ReviewChunk> chunks,
                                  List<ReviewRecommendation.ReviewFindingDraft> policyFindings,
                                  List<String> architectureContext);
}
