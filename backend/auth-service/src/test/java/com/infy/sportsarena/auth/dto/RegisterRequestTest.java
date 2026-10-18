package com.infy.sportsarena.auth.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RegisterRequestTest {

    @Test
    void accessorsEqualityHashCodeAndStringRepresentationWork() {
        RegisterRequest request = request("Jane Doe", "jane@example.com", "secret123");
        RegisterRequest equalRequest = request("Jane Doe", "jane@example.com", "secret123");

        assertThat(request.getFullName()).isEqualTo("Jane Doe");
        assertThat(request.getEmail()).isEqualTo("jane@example.com");
        assertThat(request.getPassword()).isEqualTo("secret123");
        assertThat(request).isEqualTo(equalRequest).hasSameHashCodeAs(equalRequest);
        assertThat(request.toString()).contains("Jane Doe", "jane@example.com", "secret123");
        assertThat(request).isNotEqualTo(null).isNotEqualTo("not a register request");
    }

    @Test
    void equalityAndHashCodeHandleNullAndChangedFields() {
        RegisterRequest empty = new RegisterRequest();
        RegisterRequest anotherEmpty = new RegisterRequest();
        RegisterRequest nameOnly = request("Jane Doe", null, null);
        RegisterRequest emailOnly = request(null, "jane@example.com", null);
        RegisterRequest passwordOnly = request(null, null, "secret123");
        RegisterRequest populated = request("Jane Doe", "jane@example.com", "secret123");

        assertThat(empty).isEqualTo(anotherEmpty);
        assertThat(empty.hashCode()).isEqualTo(anotherEmpty.hashCode());
        assertThat(empty).isNotEqualTo(nameOnly).isNotEqualTo(emailOnly).isNotEqualTo(passwordOnly);
        assertThat(nameOnly).isNotEqualTo(emailOnly).isNotEqualTo(passwordOnly);
        assertThat(emailOnly).isNotEqualTo(passwordOnly);
        assertThat(populated)
                .isNotEqualTo(request("Other Name", "jane@example.com", "secret123"))
                .isNotEqualTo(request("Jane Doe", "other@example.com", "secret123"))
                .isNotEqualTo(request("Jane Doe", "jane@example.com", "different"));
    }

    private RegisterRequest request(String fullName, String email, String password) {
        RegisterRequest request = new RegisterRequest();
        request.setFullName(fullName);
        request.setEmail(email);
        request.setPassword(password);
        return request;
    }
}
