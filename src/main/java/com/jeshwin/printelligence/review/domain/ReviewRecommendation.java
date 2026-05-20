package com.jeshwin.printelligence.review.domain;

import java.util.List;

public record ReviewRecommendation(
        int riskScore,
        RiskBand riskBand,
        String executiveSummary,
        String architectureNotes,
        List<ReviewFindingDraft> findings
) {
    public record ReviewFindingDraft(
            FindingSeverity severity,
            FindingCategory category,
            String filePath,
            int lineNumber,
            String title,
            String description,
            String recommendation
    ) {
    }
}
