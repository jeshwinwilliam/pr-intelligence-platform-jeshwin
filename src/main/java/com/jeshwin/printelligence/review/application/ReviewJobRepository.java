package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.ReviewJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface ReviewJobRepository extends JpaRepository<ReviewJob, UUID> {
    Optional<ReviewJob> findByIdempotencyKey(String idempotencyKey);
}
