package com.infy.sportsarena.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AuthRequestTest {

    @Test
    void accessorsEqualityHashCodeAndStringRepresentationWork() {
        AuthRequest request = new AuthRequest();
        request.setEmail("jane@example.com");
        request.setPassword("secret123");

        AuthRequest equalRequest = new AuthRequest();
        equalRequest.setEmail("jane@example.com");
        equalRequest.setPassword("secret123");

        assertThat(request.getEmail()).isEqualTo("jane@example.com");
        assertThat(request.getPassword()).isEqualTo("secret123");
        assertThat(request).isEqualTo(equalRequest).hasSameHashCodeAs(equalRequest);
        assertThat(request.toString()).contains("jane@example.com", "secret123");
        assertThat(request).isNotEqualTo(null).isNotEqualTo("not an auth request");
    }

    @Test
    void equalityAndHashCodeHandleNullAndChangedFields() {
        AuthRequest empty = new AuthRequest();
        AuthRequest anotherEmpty = new AuthRequest();
        AuthRequest emailOnly = new AuthRequest();
        emailOnly.setEmail("jane@example.com");
        AuthRequest passwordOnly = new AuthRequest();
        passwordOnly.setPassword("secret123");

        assertThat(empty).isEqualTo(anotherEmpty);
        assertThat(empty.hashCode()).isEqualTo(anotherEmpty.hashCode());
        assertThat(empty).isNotEqualTo(emailOnly).isNotEqualTo(passwordOnly);
        assertThat(emailOnly).isNotEqualTo(passwordOnly);

        AuthRequest differentEmail = new AuthRequest();
        differentEmail.setEmail("other@example.com");
        differentEmail.setPassword("secret123");
        AuthRequest differentPassword = new AuthRequest();
        differentPassword.setEmail("jane@example.com");
        differentPassword.setPassword("different");

        AuthRequest populated = new AuthRequest();
        populated.setEmail("jane@example.com");
        populated.setPassword("secret123");
        assertThat(populated).isNotEqualTo(differentEmail).isNotEqualTo(differentPassword);
    }
}
