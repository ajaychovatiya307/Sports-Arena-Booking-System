package com.infy.sportsarena.user.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserConfigTest {
    @Test
    void createsConfiguredBeans() {
        assertThat(new AppConfig().modelMapper()).isNotNull();
        assertThat(new RestTemplateConfig().restTemplate()).isNotNull();
    }
}
