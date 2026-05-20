package com.jeshwin.printelligence.review.infra.vector;

import java.util.List;

public interface KnowledgeRetrievalGateway {
    List<String> fetchContext(String repositoryName, String topic);
}
