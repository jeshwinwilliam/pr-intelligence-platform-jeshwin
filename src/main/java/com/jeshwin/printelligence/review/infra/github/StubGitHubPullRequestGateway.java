package com.jeshwin.printelligence.review.infra.github;

import com.jeshwin.printelligence.review.domain.PullRequestSnapshot;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class StubGitHubPullRequestGateway implements GitHubPullRequestGateway {

    @Override
    public PullRequestSnapshot fetch(String repositoryName, int pullRequestNumber) {
        return new PullRequestSnapshot(
                repositoryName,
                pullRequestNumber,
                "Stabilize checkout pipeline and auth middleware",
                "octo-dev",
                List.of(
                        new PullRequestSnapshot.ChangedFile(
                                "src/main/java/com/example/security/AuthFilter.java",
                                "java",
                                48,
                                9,
                                """
                                + if (request.getHeader("X-Internal-Auth") != null) { return true; }
                                + String token = request.getHeader("Authorization");
                                + if (token != null && token.startsWith("Bearer ")) { validate(token); }
                                """
                        ),
                        new PullRequestSnapshot.ChangedFile(
                                "src/main/java/com/example/controller/CheckoutController.java",
                                "java",
                                32,
                                6,
                                """
                                + public ResponseEntity<String> checkout(@RequestBody CheckoutRequest request) {
                                +   logger.info("checkout request={}", request);
                                +   paymentClient.charge(request.cardNumber(), request.amount());
                                """
                        ),
                        new PullRequestSnapshot.ChangedFile(
                                "src/main/resources/application.yml",
                                "yaml",
                                6,
                                1,
                                """
                                + security:
                                +   bypassPaths: /internal/**
                                """
                        )
                )
        );
    }
}
