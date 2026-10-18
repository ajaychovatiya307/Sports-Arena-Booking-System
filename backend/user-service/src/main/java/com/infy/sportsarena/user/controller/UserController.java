package com.infy.sportsarena.user.controller;

import com.infy.sportsarena.user.dto.CustomerProfileDTO;
import com.infy.sportsarena.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired private UserService userService;

    @PostMapping("/internal")
    public ResponseEntity<CustomerProfileDTO> createInternal(@RequestBody CustomerProfileDTO dto) {
        return ResponseEntity.ok(userService.createInternal(dto));
    }

    @GetMapping("/me")
    public ResponseEntity<CustomerProfileDTO> me(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(userService.getByUserId(userId));
    }

    @PutMapping("/me")
    public ResponseEntity<CustomerProfileDTO> updateMe(@RequestHeader("X-User-Id") String userId,
                                                      @Valid @RequestBody CustomerProfileDTO dto) {
        return ResponseEntity.ok(userService.updateProfile(userId, dto));
    }

    @GetMapping("/{userId}")
    public ResponseEntity<CustomerProfileDTO> getByUserId(@RequestHeader("X-User-Id") String requester,
                                                         @RequestHeader("X-User-Role") String role,
                                                         @PathVariable String userId) {
        if (!"ADMIN".equals(role) && !requester.equals(userId)) {
            throw new RuntimeException("Unauthorized to view this profile");
        }
        return ResponseEntity.ok(userService.getByUserId(userId));
    }

    @GetMapping
    public ResponseEntity<Page<CustomerProfileDTO>> listAll(
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "userId") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        return ResponseEntity.ok(userService.listAll(q, page, size, sortBy, direction));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Long>> stats(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        return ResponseEntity.ok(Map.of("totalCustomers", userService.count()));
    }
}
