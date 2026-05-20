package com.jeshwin.printelligence.review.api;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record CreateReviewRequest(
        @NotBlank String repositoryName,
        @Min(1) int pullRequestNumber,
        @NotBlank String author,
        @NotBlank String baseBranch,
        @NotBlank String headBranch,
        @NotBlank String policyPack
) {
}
