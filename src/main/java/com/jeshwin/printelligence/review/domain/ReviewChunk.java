package com.jeshwin.printelligence.review.domain;

import java.util.List;

public record ReviewChunk(
        String chunkId,
        String reviewLane,
        List<PullRequestSnapshot.ChangedFile> files,
        int estimatedTokens
) {
}
