package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import com.jeshwin.printelligence.review.domain.ReviewFinding;
import com.jeshwin.printelligence.review.domain.ReviewJob;
import com.jeshwin.printelligence.review.domain.ReviewRecommendation;
import com.jeshwin.printelligence.review.domain.ReviewStatus;
import com.jeshwin.printelligence.review.infra.ai.AiReviewGateway;
import com.jeshwin.printelligence.review.infra.events.ReviewEventPublisher;
import com.jeshwin.printelligence.review.infra.github.GitHubPullRequestGateway;
import com.jeshwin.printelligence.review.infra.policy.PolicyRuleEngine;
import com.jeshwin.printelligence.review.infra.vector.KnowledgeRetrievalGateway;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;

@Component
public class ReviewWorker {

    private final ReviewJobRepository reviewJobRepository;
    private final ReviewFindingRepository reviewFindingRepository;
    private final ReviewChunker reviewChunker;
    private final GitHubPullRequestGateway gitHubPullRequestGateway;
    private final PolicyRuleEngine policyRuleEngine;
    private final KnowledgeRetrievalGateway knowledgeRetrievalGateway;
    private final AiReviewGateway aiReviewGateway;
    private final ReviewEventPublisher reviewEventPublisher;

    public ReviewWorker(ReviewJobRepository reviewJobRepository,
                        ReviewFindingRepository reviewFindingRepository,
                        ReviewChunker reviewChunker,
                        GitHubPullRequestGateway gitHubPullRequestGateway,
                        PolicyRuleEngine policyRuleEngine,
                        KnowledgeRetrievalGateway knowledgeRetrievalGateway,
                        AiReviewGateway aiReviewGateway,
                        ReviewEventPublisher reviewEventPublisher) {
        this.reviewJobRepository = reviewJobRepository;
        this.reviewFindingRepository = reviewFindingRepository;
        this.reviewChunker = reviewChunker;
        this.gitHubPullRequestGateway = gitHubPullRequestGateway;
        this.policyRuleEngine = policyRuleEngine;
        this.knowledgeRetrievalGateway = knowledgeRetrievalGateway;
        this.aiReviewGateway = aiReviewGateway;
        this.reviewEventPublisher = reviewEventPublisher;
    }

    @Async("reviewExecutor")
    @Transactional
    public void analyzeAsync(UUID reviewJobId) {
        ReviewJob job = reviewJobRepository.findById(reviewJobId)
                .orElseThrow(() -> new EntityNotFoundException("Review job not found: " + reviewJobId));

        if (job.getStatus() == ReviewStatus.COMPLETED) {
            return;
        }

        try {
            job.markIngesting();
            PullRequestSnapshot snapshot = gitHubPullRequestGateway.fetch(job.getRepositoryName(), job.getPullRequestNumber());
            job.markAnalyzing();

            List<ReviewChunk> chunks = reviewChunker.chunk(snapshot);
            List<ReviewRecommendation.ReviewFindingDraft> policyFindings = policyRuleEngine.analyze(snapshot, job.getPolicyPack());
            List<String> context = Stream.of(
                            knowledgeRetrievalGateway.fetchContext(job.getRepositoryName(), "architecture"),
                            knowledgeRetrievalGateway.fetchContext(job.getRepositoryName(), "security"),
                            knowledgeRetrievalGateway.fetchContext(job.getRepositoryName(), "performance"))
                    .flatMap(List::stream)
                    .toList();

            ReviewRecommendation recommendation = aiReviewGateway.generate(snapshot, chunks, policyFindings, context);

            reviewFindingRepository.deleteByReviewJobId(job.getId());
            reviewFindingRepository.saveAll(recommendation.findings().stream()
                    .map(finding -> new ReviewFinding(
                            job.getId(),
                            finding.severity(),
                            finding.category(),
                            finding.filePath(),
                            finding.lineNumber(),
                            finding.title(),
                            finding.description(),
                            finding.recommendation()
                    ))
                    .toList());

            job.markCompleted(
                    recommendation.riskScore(),
                    recommendation.riskBand(),
                    recommendation.executiveSummary(),
                    recommendation.architectureNotes()
            );
            reviewEventPublisher.publishCompleted(job);
        } catch (RuntimeException ex) {
            job.markFailed("Review failed: " + ex.getMessage());
        }
    }
}
