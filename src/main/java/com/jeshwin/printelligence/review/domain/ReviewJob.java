package com.jeshwin.printelligence.review.domain;

import com.jeshwin.printelligence.common.domain.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;

@Entity
@Table(name = "review_jobs")
public class ReviewJob extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    @Column(nullable = false)
    private String repositoryName;

    @Column(nullable = false)
    private Integer pullRequestNumber;

    @Column(nullable = false)
    private String author;

    @Column(nullable = false)
    private String baseBranch;

    @Column(nullable = false)
    private String headBranch;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PolicyPack policyPack;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReviewStatus status;

    @Column
    private Integer riskScore;

    @Enumerated(EnumType.STRING)
    @Column
    private RiskBand riskBand;

    @Column(length = 1200)
    private String executiveSummary;

    @Lob
    private String architectureNotes;

    @Lob
    private String failureReason;

    protected ReviewJob() {
    }

    public ReviewJob(String idempotencyKey,
                     String repositoryName,
                     Integer pullRequestNumber,
                     String author,
                     String baseBranch,
                     String headBranch,
                     PolicyPack policyPack) {
        this.idempotencyKey = idempotencyKey;
        this.repositoryName = repositoryName;
        this.pullRequestNumber = pullRequestNumber;
        this.author = author;
        this.baseBranch = baseBranch;
        this.headBranch = headBranch;
        this.policyPack = policyPack;
        this.status = ReviewStatus.ACCEPTED;
    }

    public void markIngesting() {
        status = ReviewStatus.INGESTING;
    }

    public void markAnalyzing() {
        status = ReviewStatus.ANALYZING;
    }

    public void markCompleted(int riskScore, RiskBand riskBand, String executiveSummary, String architectureNotes) {
        status = ReviewStatus.COMPLETED;
        this.riskScore = riskScore;
        this.riskBand = riskBand;
        this.executiveSummary = executiveSummary;
        this.architectureNotes = architectureNotes;
        this.failureReason = null;
    }

    public void markFailed(String failureReason) {
        status = ReviewStatus.FAILED;
        this.failureReason = failureReason;
    }

    public String getIdempotencyKey() {
        return idempotencyKey;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public Integer getPullRequestNumber() {
        return pullRequestNumber;
    }

    public String getAuthor() {
        return author;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public PolicyPack getPolicyPack() {
        return policyPack;
    }

    public ReviewStatus getStatus() {
        return status;
    }

    public Integer getRiskScore() {
        return riskScore;
    }

    public RiskBand getRiskBand() {
        return riskBand;
    }

    public String getExecutiveSummary() {
        return executiveSummary;
    }

    public String getArchitectureNotes() {
        return architectureNotes;
    }

    public String getFailureReason() {
        return failureReason;
    }
}
