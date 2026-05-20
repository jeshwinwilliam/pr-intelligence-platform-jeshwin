package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.ReviewFinding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewFindingRepository extends JpaRepository<ReviewFinding, UUID> {
    List<ReviewFinding> findByReviewJobId(UUID reviewJobId);
    void deleteByReviewJobId(UUID reviewJobId);
}
