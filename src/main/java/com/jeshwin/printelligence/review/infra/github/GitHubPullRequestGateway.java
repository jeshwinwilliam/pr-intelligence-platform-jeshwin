package com.jeshwin.printelligence.review.infra.github;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;

public interface GitHubPullRequestGateway {
    PullRequestSnapshot fetch(String repositoryName, int pullRequestNumber);
}
