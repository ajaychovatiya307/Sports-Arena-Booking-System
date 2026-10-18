package com.infy.sportsarena.gateway.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class GatewayConfig {

    /**
     * Load-balanced WebClient builder so the role-resolution filter can reach
     * auth-service through Consul service discovery (lb://auth-service).
     */
    @Bean
    @LoadBalanced
    public WebClient.Builder loadBalancedWebClientBuilder() {
        return WebClient.builder();
    }
}
