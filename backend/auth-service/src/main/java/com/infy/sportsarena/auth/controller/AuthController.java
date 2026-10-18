package com.infy.sportsarena.auth.controller;

import com.infy.sportsarena.auth.dto.AuthRequest;
import com.infy.sportsarena.auth.dto.RegisterRequest;
import com.infy.sportsarena.auth.entity.UserCredentials;
import com.infy.sportsarena.auth.repository.UserRepository;
import com.infy.sportsarena.auth.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired private AuthService authService;
    @Autowired private UserRepository userRepository;

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(@Valid @RequestBody RegisterRequest request) {
        String msg = authService.register(request);
        Map<String, String> body = new HashMap<>();
        body.put("message", msg);
        return new ResponseEntity<>(body, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, String>> login(@Valid @RequestBody AuthRequest request) {
        UserCredentials u = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid email or password"));
        if (!request.getPassword().equals(u.getPassword())) {
            throw new RuntimeException("Invalid email or password");
        }
        // Self-heal: make sure a customer_profile row exists for this user.
        // (Covers users whose profile creation was deferred when user-service was down.)
        authService.ensureProfile(u);

        Map<String, String> body = new HashMap<>();
        body.put("userId", u.getUserId());
        body.put("fullName", u.getFullName());
        body.put("email", u.getEmail());
        body.put("role", u.getRole());
        return ResponseEntity.ok(body);
    }

    /**
     * Authoritative role lookup used by the API Gateway to prevent clients from
     * spoofing their role via a tampered X-User-Role header. Returns the role
     * stored in auth_db for the given userId.
     */
    @GetMapping("/resolve")
    public ResponseEntity<Map<String, String>> resolve(@RequestParam String userId) {
        Map<String, String> body = new HashMap<>();
        userRepository.findByUserId(userId).ifPresent(u -> {
            body.put("userId", u.getUserId());
            body.put("role", u.getRole());
        });
        return ResponseEntity.ok(body);
    }

    /**
     * Internal endpoint called by user-service when a customer updates their
     * profile name, so the name stays in sync in auth_db (and on the dashboard).
     */
    @PutMapping("/internal/profile")
    public ResponseEntity<Map<String, String>> updateName(@RequestBody Map<String, String> body) {
        authService.updateFullName(body.get("userId"), body.get("fullName"));
        return ResponseEntity.ok(Map.of("message", "Name synchronized"));
    }
}
