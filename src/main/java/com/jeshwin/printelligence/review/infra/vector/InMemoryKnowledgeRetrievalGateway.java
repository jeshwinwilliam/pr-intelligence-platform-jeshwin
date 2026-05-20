package com.jeshwin.printelligence.review.infra.vector;

import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class InMemoryKnowledgeRetrievalGateway implements KnowledgeRetrievalGateway {

    private static final Map<String, List<String>> KNOWLEDGE = Map.of(
            "security", List.of(
                    "Internal headers must never bypass authentication without gateway verification.",
                    "Payment requests must avoid logging raw PII or account data."
            ),
            "architecture", List.of(
                    "Controllers should stay orchestration-thin and delegate sensitive workflows to dedicated services.",
                    "Cross-cutting security policy belongs in middleware and centrally managed configuration."
            ),
            "performance", List.of(
                    "Large pull requests should be chunked to preserve review latency and model token budgets."
            )
    );

    @Override
    public List<String> fetchContext(String repositoryName, String topic) {
        return KNOWLEDGE.getOrDefault(topic, List.of("No indexed guideline found for " + topic + " in " + repositoryName + "."));
    }
}
