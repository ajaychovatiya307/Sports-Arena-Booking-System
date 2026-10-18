package com.infy.sportsarena.catalog;

import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;

import static org.mockito.Mockito.mockStatic;

class CatalogServiceApplicationTest {
    @Test
    void mainDelegatesToSpringApplication() {
        new CatalogServiceApplication();
        try (var springApplication = mockStatic(SpringApplication.class)) {
            CatalogServiceApplication.main(new String[0]);
            springApplication.verify(() -> SpringApplication.run(CatalogServiceApplication.class));
        }
    }
}
