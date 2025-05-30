package com.ecommerce.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.support.HttpHeaders;
import org.springframework.lang.NonNull;

@Configuration
@EnableElasticsearchRepositories(basePackages = "com.ecommerce.elasticsearch.repository")
public class ElasticsearchConfig extends ElasticsearchConfiguration {

    @Value("${spring.elasticsearch.uris}")
    private String elasticsearchUris;

    // Read the API Key from application.properties
    @Value("${spring.elasticsearch.api-key:#{null}}")
    private String apiKey;

    @Override
    @NonNull
    public ClientConfiguration clientConfiguration() {
        var builder = ClientConfiguration.builder()
                .connectedTo(elasticsearchUris.split(","))
                .withConnectTimeout(java.time.Duration.ofSeconds(5))
                .withSocketTimeout(java.time.Duration.ofSeconds(10));

        // Configure the client to use the API Key for authentication.
        if (apiKey != null && !apiKey.isEmpty()) {
            HttpHeaders defaultHeaders = new HttpHeaders();
            defaultHeaders.add("Authorization", "ApiKey " + apiKey);
            builder.withDefaultHeaders(defaultHeaders);
        }

        return builder.build();
    }
}