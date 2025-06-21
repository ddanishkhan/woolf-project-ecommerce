package com.ecommerce.notificationservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateConfig {

    @Value("${user.service.url}")
    private String userServiceUrl;

    @Value("${internal.api.secret}")
    private String internalApiSecret;


    @Bean
    public RestTemplate serviceRestTemplate(RestTemplateBuilder builder) {
        // 1. Fetch the service token first.
        String serviceToken = getServiceToken();

        // 2. Create an interceptor to add the token to every request.
        ClientHttpRequestInterceptor interceptor = (request, body, execution) -> {
            request.getHeaders().add(HttpHeaders.AUTHORIZATION, "Bearer " + serviceToken);
            return execution.execute(request, body);
        };

        return builder.additionalInterceptors(interceptor).build();
    }

    // Helper method to call the user-management-service on startup to get a token.
    private String getServiceToken() {
        RestTemplate restTemplate = new RestTemplate();
        String url = userServiceUrl + "/api/internal/auth/service-token";

        org.springframework.http.HttpEntity<String> entity = new org.springframework.http.HttpEntity<>(
                createHeaders(internalApiSecret)
        );

        return restTemplate.postForObject(url, entity, String.class);
    }

    private HttpHeaders createHeaders(String secret) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Internal-Secret", secret);
        return headers;
    }

}
