package com.infy.sportsarena.auth.entity;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class UserCredentialsTest {

    @Test
    void accessorsEqualityHashCodeAndStringRepresentationWork() {
        UserCredentials user = user(1L, "U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");
        UserCredentials equalUser = user(1L, "U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");

        assertThat(user.getId()).isEqualTo(1L);
        assertThat(user.getUserId()).isEqualTo("U1");
        assertThat(user.getFullName()).isEqualTo("Jane Doe");
        assertThat(user.getEmail()).isEqualTo("jane@example.com");
        assertThat(user.getPassword()).isEqualTo("secret123");
        assertThat(user.getRole()).isEqualTo("CUSTOMER");
        assertThat(user).isEqualTo(equalUser).hasSameHashCodeAs(equalUser);
        assertThat(user.toString()).contains("U1", "Jane Doe", "jane@example.com", "CUSTOMER");
        assertThat(user).isNotEqualTo(null).isNotEqualTo("not a user");
    }

    @Test
    void equalityAndHashCodeHandleNullAndChangedFields() {
        UserCredentials empty = new UserCredentials();
        UserCredentials anotherEmpty = new UserCredentials();
        UserCredentials populated = user(1L, "U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");

        assertThat(empty).isEqualTo(anotherEmpty);
        assertThat(empty.hashCode()).isEqualTo(anotherEmpty.hashCode());
        assertThat(empty).isNotEqualTo(populated);

        assertThat(populated)
                .isNotEqualTo(user(2L, "U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER"))
                .isNotEqualTo(user(1L, "U2", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER"))
                .isNotEqualTo(user(1L, "U1", "Other Name", "jane@example.com", "secret123", "CUSTOMER"))
                .isNotEqualTo(user(1L, "U1", "Jane Doe", "other@example.com", "secret123", "CUSTOMER"))
                .isNotEqualTo(user(1L, "U1", "Jane Doe", "jane@example.com", "different", "CUSTOMER"))
                .isNotEqualTo(user(1L, "U1", "Jane Doe", "jane@example.com", "secret123", "ADMIN"));
    }

    @Test
    void equalityHandlesNullFieldsAgainstPopulatedFields() {
        UserCredentials populated = user(1L, "U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");
        UserCredentials idOnly = user(1L, null, null, null, null, null);
        UserCredentials userIdOnly = user(null, "U1", null, null, null, null);
        UserCredentials fullNameOnly = user(null, null, "Jane Doe", null, null, null);
        UserCredentials emailOnly = user(null, null, null, "jane@example.com", null, null);
        UserCredentials passwordOnly = user(null, null, null, null, "secret123", null);
        UserCredentials roleOnly = user(null, null, null, null, null, "CUSTOMER");

        assertThat(populated).isNotEqualTo(idOnly).isNotEqualTo(userIdOnly)
                .isNotEqualTo(fullNameOnly).isNotEqualTo(emailOnly)
                .isNotEqualTo(passwordOnly).isNotEqualTo(roleOnly);
        assertThat(idOnly.hashCode()).isNotEqualTo(populated.hashCode());
    }

    private UserCredentials user(Long id, String userId, String fullName, String email, String password, String role) {
        UserCredentials user = new UserCredentials();
        user.setId(id);
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
