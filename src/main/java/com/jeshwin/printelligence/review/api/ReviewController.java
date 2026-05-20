package com.jeshwin.printelligence.review.api;

import com.jeshwin.printelligence.review.application.ReviewOrchestrationService;
import com.jeshwin.printelligence.review.domain.PolicyPack;
import com.jeshwin.printelligence.review.domain.ReviewJob;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/reviews")
public class ReviewController {

    private final ReviewOrchestrationService reviewOrchestrationService;

    public ReviewController(ReviewOrchestrationService reviewOrchestrationService) {
        this.reviewOrchestrationService = reviewOrchestrationService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ReviewJobResponse create(@Valid @RequestBody CreateReviewRequest request) {
        ReviewJob job = reviewOrchestrationService.submit(
                request.repositoryName(),
                request.pullRequestNumber(),
                request.author(),
                request.baseBranch(),
                request.headBranch(),
                PolicyPack.valueOf(request.policyPack().toUpperCase())
        );
        return ReviewJobResponse.from(job);
    }

    @GetMapping
    public List<ReviewJobResponse> list() {
        return reviewOrchestrationService.listJobs().stream().map(ReviewJobResponse::from).toList();
    }

    @GetMapping("/{reviewJobId}")
    public ReviewDetailResponse get(@PathVariable UUID reviewJobId) {
        return new ReviewDetailResponse(
                ReviewJobResponse.from(reviewOrchestrationService.getJob(reviewJobId)),
                reviewOrchestrationService.getFindings(reviewJobId).stream()
                        .map(ReviewFindingResponse::from)
                        .toList()
        );
    }
}
