package com.infy.sportsarena.auth.service;

import com.infy.sportsarena.auth.dto.RegisterRequest;
import com.infy.sportsarena.auth.entity.UserCredentials;
import com.infy.sportsarena.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setFullName("Jane Doe");
        registerRequest.setEmail("jane@example.com");
        registerRequest.setPassword("secret123");
    }

    @Test
    void register_savesNewCustomerAndReturnsGeneratedUserId() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(false);
        when(userRepository.countByRole("CUSTOMER")).thenReturn(3L);
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        String message = authService.register(registerRequest);

        assertThat(message).contains("U4");
        verify(userRepository).save(argThat(u -> "U4".equals(u.getUserId())
                && "CUSTOMER".equals(u.getRole())
                && "jane@example.com".equals(u.getEmail())));
        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyRegistered() {
        when(userRepository.existsByEmail("jane@example.com")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
        verify(userRepository, never()).save(any());
    }

    @Test
    void ensureProfile_doesNothingForNullUser() {
        authService.ensureProfile(null);
        verifyNoInteractions(restTemplate);
    }

    @Test
    void ensureProfile_doesNothingForAdminRole() {
        UserCredentials admin = new UserCredentials();
        admin.setRole("ADMIN");

        authService.ensureProfile(admin);

        verifyNoInteractions(restTemplate);
    }

    @Test
    void ensureProfile_createsProfileForCustomer() {
        UserCredentials customer = new UserCredentials();
        customer.setUserId("U1");
        customer.setFullName("Jane Doe");
        customer.setEmail("jane@example.com");
        customer.setRole("CUSTOMER");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenReturn(ResponseEntity.ok("ok"));

        authService.ensureProfile(customer);

        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void ensureProfile_swallowsDownstreamFailureForCustomer() {
        UserCredentials customer = new UserCredentials();
        customer.setUserId("U1");
        customer.setFullName("Jane Doe");
        customer.setEmail("jane@example.com");
        customer.setRole("CUSTOMER");
        when(restTemplate.postForEntity(anyString(), any(), eq(String.class)))
                .thenThrow(new RuntimeException("user-service down"));

        // Must not propagate the exception.
        authService.ensureProfile(customer);

        verify(restTemplate).postForEntity(anyString(), any(), eq(String.class));
    }

    @Test
    void updateFullName_ignoresBlankName() {
        authService.updateFullName("U1", "  ");
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateFullName_ignoresNullUserId() {
        authService.updateFullName(null, "New Name");
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateFullName_ignoresNullName() {
        authService.updateFullName("U1", null);
        verifyNoInteractions(userRepository);
    }

    @Test
    void updateFullName_updatesWhenUserExists() {
        UserCredentials u = new UserCredentials();
        u.setUserId("U1");
        u.setFullName("Old Name");
        when(userRepository.findByUserId("U1")).thenReturn(Optional.of(u));

        authService.updateFullName("U1", "New Name");

        assertThat(u.getFullName()).isEqualTo("New Name");
        verify(userRepository).save(u);
    }

    @Test
    void updateFullName_noOpWhenUserNotFound() {
        when(userRepository.findByUserId("U99")).thenReturn(Optional.empty());

        authService.updateFullName("U99", "New Name");

        verify(userRepository, never()).save(any());
    }
}
