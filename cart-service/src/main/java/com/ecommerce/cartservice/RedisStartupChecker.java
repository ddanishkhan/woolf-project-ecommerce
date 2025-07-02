package com.ecommerce.cartservice;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisStartupChecker implements ApplicationRunner {

    private final RedisConnectionFactory connectionFactory;
    private final ApplicationContext applicationContext;

    @Override
    public void run(ApplicationArguments args) {
        log.info("Attempting to connect to Redis during application startup to verify connectivity...");
        try {
            // Attempt a PING
            connectionFactory.getConnection().ping();
            log.info("Successfully connected to Redis!");
        } catch (Exception e) {
            log.error("FATAL ERROR: Could not connect to Redis at startup. Application will terminate.");
            log.error("Reason: {}", e.getMessage(), e);
            // Ensure the application stops gracefully
            SpringApplication.exit(applicationContext, () -> 1); // Exit with a non-zero status code
        }
    }
}
