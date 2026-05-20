package com.jeshwin.printelligence.review.application;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import com.jeshwin.printelligence.review.domain.ReviewChunk;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Component
public class ReviewChunker {

    public List<ReviewChunk> chunk(PullRequestSnapshot snapshot) {
        List<ReviewChunk> chunks = new ArrayList<>();
        int index = 1;
        for (PullRequestSnapshot.ChangedFile file : snapshot.changedFiles()) {
            chunks.add(new ReviewChunk(
                    "chunk-" + index++,
                    classifyLane(file),
                    List.of(file),
                    Math.max(40, file.patch().length() / 4)
            ));
        }
        return chunks;
    }

    private String classifyLane(PullRequestSnapshot.ChangedFile file) {
        String path = file.path().toLowerCase(Locale.ROOT);
        if (path.contains("auth") || path.contains("security")) {
            return "trust-boundary";
        }
        if (path.contains("controller") || path.contains("api")) {
            return "request-edge";
        }
        if (path.endsWith(".yml") || path.endsWith(".yaml") || path.contains("config")) {
            return "runtime-policy";
        }
        return "core-change";
    }
}
