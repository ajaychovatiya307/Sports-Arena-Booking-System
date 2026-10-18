package com.infy.sportsarena.auth.controller;

import com.infy.sportsarena.auth.dto.AuthRequest;
import com.infy.sportsarena.auth.dto.RegisterRequest;
import com.infy.sportsarena.auth.entity.UserCredentials;
import com.infy.sportsarena.auth.repository.UserRepository;
import com.infy.sportsarena.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerReturnsCreatedResponse() {
        RegisterRequest request = new RegisterRequest();
        request.setFullName("Jane Doe");
        request.setEmail("jane@example.com");
        request.setPassword("secret123");
        when(authService.register(request)).thenReturn("Registered successfully. Your user id is U1");

        ResponseEntity<Map<String, String>> response = authController.register(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).containsEntry("message", "Registered successfully. Your user id is U1");
        verify(authService).register(request);
    }

    @Test
    void loginReturnsUserAndEnsuresCustomerProfile() {
        AuthRequest request = new AuthRequest();
        request.setEmail("jane@example.com");
        request.setPassword("secret123");
        UserCredentials user = user("U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = authController.login(request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).containsEntry("userId", "U1")
                .containsEntry("fullName", "Jane Doe")
                .containsEntry("email", "jane@example.com")
                .containsEntry("role", "CUSTOMER");
        verify(authService).ensureProfile(user);
    }

    @Test
    void loginRejectsUnknownEmail() {
        AuthRequest request = new AuthRequest();
        request.setEmail("missing@example.com");
        request.setPassword("secret123");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authController.login(request));

        assertThat(exception).hasMessage("Invalid email or password");
        verifyNoInteractions(authService);
    }

    @Test
    void loginRejectsIncorrectPassword() {
        AuthRequest request = new AuthRequest();
        request.setEmail("jane@example.com");
        request.setPassword("wrong");
        UserCredentials user = user("U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");
        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authController.login(request));

        assertThat(exception).hasMessage("Invalid email or password");
        verifyNoInteractions(authService);
    }

    @Test
    void resolveReturnsUserIdentityWhenUserExists() {
        UserCredentials user = user("U1", "Jane Doe", "jane@example.com", "secret123", "CUSTOMER");
        when(userRepository.findByUserId("U1")).thenReturn(Optional.of(user));

        ResponseEntity<Map<String, String>> response = authController.resolve("U1");

        assertThat(response.getBody()).containsEntry("userId", "U1").containsEntry("role", "CUSTOMER");
    }

    @Test
    void resolveReturnsEmptyBodyWhenUserDoesNotExist() {
        when(userRepository.findByUserId("U99")).thenReturn(Optional.empty());

        ResponseEntity<Map<String, String>> response = authController.resolve("U99");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEmpty();
    }

    @Test
    void updateNameSynchronizesAuthNameAndReturnsMessage() {
        Map<String, String> body = new HashMap<>();
        body.put("userId", "U1");
        body.put("fullName", "Jane Smith");

        ResponseEntity<Map<String, String>> response = authController.updateName(body);

        assertThat(response.getBody()).containsEntry("message", "Name synchronized");
        verify(authService).updateFullName("U1", "Jane Smith");
    }

    private UserCredentials user(String userId, String fullName, String email, String password, String role) {
        UserCredentials user = new UserCredentials();
        user.setUserId(userId);
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(password);
        user.setRole(role);
        return user;
    }
}
