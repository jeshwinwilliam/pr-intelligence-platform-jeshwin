package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.PolicyPack;
import com.jeshwin.printelligence.review.domain.ReviewFinding;
import com.jeshwin.printelligence.review.domain.ReviewJob;
import com.jeshwin.printelligence.review.infra.events.ReviewEventPublisher;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

@Service
public class ReviewOrchestrationService {

    private final ReviewJobRepository reviewJobRepository;
    private final ReviewFindingRepository reviewFindingRepository;
    private final ReviewWorker reviewWorker;
    private final ReviewEventPublisher reviewEventPublisher;

    public ReviewOrchestrationService(ReviewJobRepository reviewJobRepository,
                                      ReviewFindingRepository reviewFindingRepository,
                                      ReviewWorker reviewWorker,
                                      ReviewEventPublisher reviewEventPublisher) {
        this.reviewJobRepository = reviewJobRepository;
        this.reviewFindingRepository = reviewFindingRepository;
        this.reviewWorker = reviewWorker;
        this.reviewEventPublisher = reviewEventPublisher;
    }

    @Transactional
    public ReviewJob submit(String repositoryName,
                            int pullRequestNumber,
                            String author,
                            String baseBranch,
                            String headBranch,
                            PolicyPack policyPack) {
        String key = createIdempotencyKey(repositoryName, pullRequestNumber, headBranch, policyPack);
        return reviewJobRepository.findByIdempotencyKey(key)
                .orElseGet(() -> {
                    ReviewJob job = reviewJobRepository.save(new ReviewJob(
                            key,
                            repositoryName,
                            pullRequestNumber,
                            author,
                            baseBranch,
                            headBranch,
                            policyPack
                    ));
                    reviewEventPublisher.publishAccepted(job);
                    reviewWorker.analyzeAsync(job.getId());
                    return job;
                });
    }

    @Transactional(readOnly = true)
    public ReviewJob getJob(UUID reviewJobId) {
        return reviewJobRepository.findById(reviewJobId)
                .orElseThrow(() -> new EntityNotFoundException("Review job not found: " + reviewJobId));
    }

    @Transactional(readOnly = true)
    public List<ReviewJob> listJobs() {
        return reviewJobRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<ReviewFinding> getFindings(UUID reviewJobId) {
        return reviewFindingRepository.findByReviewJobId(reviewJobId);
    }

    private String createIdempotencyKey(String repositoryName, int pullRequestNumber, String headBranch, PolicyPack policyPack) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashed = digest.digest((repositoryName + "|" + pullRequestNumber + "|" + headBranch + "|" + policyPack.name())
                    .getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hashed);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Unable to generate idempotency key", ex);
        }
    }
}
