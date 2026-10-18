package com.infy.sportsarena.gateway;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class GatewayServiceApplicationTest {
    @Test
    void mainDelegatesToSpringApplication() {
        new GatewayServiceApplication();
        try (var springApplication = mockStatic(SpringApplication.class)) {
            GatewayServiceApplication.main(new String[0]);
            springApplication.verify(() -> SpringApplication.run(GatewayServiceApplication.class));
        }
    }
}
