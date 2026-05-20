package com.jeshwin.printelligence.review.api;

import java.util.List;

public record ReviewDetailResponse(
        ReviewJobResponse job,
        List<ReviewFindingResponse> findings
) {
}
