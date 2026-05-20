package com.jeshwin.printelligence.review.api;

import com.jeshwin.printelligence.review.domain.ReviewJob;

import java.time.Instant;
import java.util.UUID;

public record ReviewJobResponse(
        UUID id,
        String repositoryName,
        Integer pullRequestNumber,
        String author,
        String baseBranch,
        String headBranch,
        String policyPack,
        String status,
        Integer riskScore,
        String riskBand,
        String executiveSummary,
        String architectureNotes,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static ReviewJobResponse from(ReviewJob job) {
        return new ReviewJobResponse(
                job.getId(),
                job.getRepositoryName(),
                job.getPullRequestNumber(),
                job.getAuthor(),
                job.getBaseBranch(),
                job.getHeadBranch(),
                job.getPolicyPack().name(),
                job.getStatus().name(),
                job.getRiskScore(),
                job.getRiskBand() == null ? null : job.getRiskBand().name(),
                job.getExecutiveSummary(),
                job.getArchitectureNotes(),
                job.getFailureReason(),
                job.getCreatedAt(),
                job.getUpdatedAt()
        );
    }
}
