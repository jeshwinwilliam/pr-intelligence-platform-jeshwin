package com.jeshwin.printelligence.review.domain;

import java.util.List;

public record PullRequestSnapshot(
        String repository,
        int pullRequestNumber,
        String title,
        String author,
        List<ChangedFile> changedFiles
) {
    public record ChangedFile(
            String path,
            String language,
            int additions,
            int deletions,
            String patch
    ) {
    }
}
