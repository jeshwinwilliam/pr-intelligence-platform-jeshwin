package com.jeshwin.printelligence.review.infra.events;

import com.jeshwin.printelligence.review.application.OutboxEvent;
import com.jeshwin.printelligence.review.application.OutboxEventRepository;
import com.jeshwin.printelligence.review.domain.ReviewJob;
import org.springframework.stereotype.Component;

@Component
public class ReviewEventPublisher {

    private final OutboxEventRepository outboxEventRepository;

    public ReviewEventPublisher(OutboxEventRepository outboxEventRepository) {
        this.outboxEventRepository = outboxEventRepository;
    }

    public void publishAccepted(ReviewJob job) {
        outboxEventRepository.save(new OutboxEvent(
                "REVIEW_JOB",
                job.getId().toString(),
                "REVIEW_ACCEPTED",
                "{\"repository\":\"" + job.getRepositoryName() + "\",\"pr\":" + job.getPullRequestNumber() + "}"
        ));
    }

    public void publishCompleted(ReviewJob job) {
        outboxEventRepository.save(new OutboxEvent(
                "REVIEW_JOB",
                job.getId().toString(),
                "REVIEW_COMPLETED",
                "{\"riskScore\":" + job.getRiskScore() + ",\"riskBand\":\"" + job.getRiskBand() + "\"}"
        ));
    }
}
