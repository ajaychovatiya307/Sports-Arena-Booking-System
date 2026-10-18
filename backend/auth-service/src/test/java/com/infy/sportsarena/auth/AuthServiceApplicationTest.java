package com.infy.sportsarena.auth;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.assertThat;

class AuthServiceApplicationTest {

    @Test
    void restTemplateBeanCreatesRestTemplate() {
        assertThat(new AuthServiceApplication().restTemplate()).isInstanceOf(RestTemplate.class);
    }
}
