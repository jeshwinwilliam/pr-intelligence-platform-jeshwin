package com.jeshwin.printelligence.review.domain;

import com.jeshwin.printelligence.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "review_findings")
public class ReviewFinding extends BaseEntity {

    @Column(nullable = false)
    private UUID reviewJobId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingSeverity severity;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FindingCategory category;

    @Column(nullable = false)
    private String filePath;

    @Column(nullable = false)
    private Integer lineNumber;

    @Column(nullable = false, length = 240)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Lob
    @Column(nullable = false)
    private String recommendation;

    protected ReviewFinding() {
    }

    public ReviewFinding(UUID reviewJobId,
                         FindingSeverity severity,
                         FindingCategory category,
                         String filePath,
                         Integer lineNumber,
                         String title,
                         String description,
                         String recommendation) {
        this.reviewJobId = reviewJobId;
        this.severity = severity;
        this.category = category;
        this.filePath = filePath;
        this.lineNumber = lineNumber;
        this.title = title;
        this.description = description;
        this.recommendation = recommendation;
    }

    public UUID getReviewJobId() {
        return reviewJobId;
    }

    public FindingSeverity getSeverity() {
        return severity;
    }

    public FindingCategory getCategory() {
        return category;
    }

    public String getFilePath() {
        return filePath;
    }

    public Integer getLineNumber() {
        return lineNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getRecommendation() {
        return recommendation;
    }
}
