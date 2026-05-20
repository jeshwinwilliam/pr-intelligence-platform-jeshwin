package com.jeshwin.printelligence.review.api;

import com.jeshwin.printelligence.review.domain.ReviewFinding;

import java.util.UUID;

public record ReviewFindingResponse(
        UUID id,
        String severity,
        String category,
        String filePath,
        Integer lineNumber,
        String title,
        String description,
        String recommendation
) {
    public static ReviewFindingResponse from(ReviewFinding finding) {
        return new ReviewFindingResponse(
                finding.getId(),
                finding.getSeverity().name(),
                finding.getCategory().name(),
                finding.getFilePath(),
                finding.getLineNumber(),
                finding.getTitle(),
                finding.getDescription(),
                finding.getRecommendation()
        );
    }
}
