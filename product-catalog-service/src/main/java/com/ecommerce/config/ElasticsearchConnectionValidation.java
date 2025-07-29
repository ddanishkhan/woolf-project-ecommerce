package com.ecommerce.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ElasticsearchConnectionValidation {

    private final ElasticsearchClient elasticsearchClient;

    @Autowired
    public ElasticsearchConnectionValidation(ElasticsearchClient elasticsearchClient) {
        this.elasticsearchClient = elasticsearchClient;
    }

    @PostConstruct
    public void validateConnection() {
        try {
            if (!elasticsearchClient.ping().value()) {
                throw new IllegalStateException("Elasticsearch is not reachable.");
            }
        } catch (Exception e) {
            throw new IllegalStateException("Failed to connect to Elasticsearch.", e);
        }
    }
}
