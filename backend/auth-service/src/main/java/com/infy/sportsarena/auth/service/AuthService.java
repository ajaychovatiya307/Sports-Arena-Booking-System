package com.infy.sportsarena.auth.service;

import com.infy.sportsarena.auth.dto.RegisterRequest;
import com.infy.sportsarena.auth.entity.UserCredentials;
import com.infy.sportsarena.auth.repository.UserRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class AuthService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestTemplate restTemplate;

    public String register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email is already registered");
        }

        long customerCount = userRepository.countByRole("CUSTOMER");
        String userId = "U" + (customerCount + 1);

        UserCredentials u = new UserCredentials();
        u.setUserId(userId);
        u.setFullName(request.getFullName());
        u.setEmail(request.getEmail());
        u.setPassword(request.getPassword());
        u.setRole("CUSTOMER");
        userRepository.save(u);

        createCustomerProfile(userId, request.getFullName(), request.getEmail());
        return "Registered successfully. Your user id is " + userId;
    }

    /**
     * Ensure a customer_profile row exists for this user. Called on login so that
     * users whose profile creation was deferred (user-service was unavailable at
     * registration time) get self-healed. Idempotent and never blocks login.
     */
    public void ensureProfile(UserCredentials u) {
        if (u == null || !"CUSTOMER".equals(u.getRole())) {
            return;
        }
        try {
            createCustomerProfile(u.getUserId(), u.getFullName(), u.getEmail());
        } catch (Exception ex) {
            System.err.println("[auth-service] ensureProfile deferred for " + u.getUserId() + " : " + ex.getMessage());
        }
    }

    /**
     * Keep the user's display name in auth_db in sync when the customer updates
     * their profile in user-service.
     */
    public void updateFullName(String userId, String fullName) {
        if (userId == null || fullName == null || fullName.isBlank()) {
            return;
        }
        userRepository.findByUserId(userId).ifPresent(u -> {
            u.setFullName(fullName);
            userRepository.save(u);
        });
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "createProfileFallback")
    public void createCustomerProfile(String userId, String fullName, String email) {
        Map<String, String> body = new HashMap<>();
        body.put("userId", userId);
        body.put("fullName", fullName);
        body.put("email", email);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("X-User-Id", userId);
        headers.set("X-User-Role", "CUSTOMER");
        HttpEntity<Map<String, String>> req = new HttpEntity<>(body, headers);
        restTemplate.postForEntity("http://user-service/api/users/internal", req, String.class);
    }

    @SuppressWarnings("unused")
    private void createProfileFallback(String userId, String fullName, String email, Throwable ex) {
        // Log and swallow — customer can complete profile later on first login
        System.err.println("[auth-service] user-service unavailable, profile deferred for " + userId + " : " + ex.getMessage());
    }
}
