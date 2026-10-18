package com.infy.sportsarena.catalog.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AppConfigTest {
    @Test
    void createsModelMapperAndRestTemplateBeans() {
        AppConfig config = new AppConfig();
        assertThat(config.modelMapper()).isNotNull();
        assertThat(config.restTemplate()).isNotNull();
    }
}
