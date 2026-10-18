package com.infy.sportsarena.gateway.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class GatewayConfigTest {
    @Test
    void createsWebClientBuilder() {
        assertThat(new GatewayConfig().loadBalancedWebClientBuilder()).isNotNull();
    }
}
