package com.infy.sportsarena.booking.controller;

import com.infy.sportsarena.booking.dto.RatingDTO;
import com.infy.sportsarena.booking.service.RatingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/ratings")
public class RatingController {

    @Autowired private RatingService service;

    @PostMapping
    public ResponseEntity<RatingDTO> submit(@RequestHeader("X-User-Id") String userId,
                                            @Valid @RequestBody RatingDTO dto) {
        return ResponseEntity.ok(service.submit(userId, dto));
    }

    @GetMapping("/me")
    public ResponseEntity<List<RatingDTO>> mine(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(service.myRatings(userId));
    }

    @GetMapping("/package/{packageId}")
    public ResponseEntity<Map<String, Object>> aggregateForPackage(@PathVariable Long packageId) {
        return ResponseEntity.ok(service.aggregateForPackage(packageId));
    }

    // SRS US12: Administrator shall be able to view individual ratings per arena package.
    @GetMapping("/package/{packageId}/list")
    public ResponseEntity<List<RatingDTO>> ratingsByPackage(@RequestHeader("X-User-Role") String role,
                                                            @PathVariable Long packageId) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        return ResponseEntity.ok(service.ratingsByPackage(packageId));
    }

    // All individual ratings (admin) for drill-down reporting.
    @GetMapping("/all")
    public ResponseEntity<List<RatingDTO>> allRatings(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        return ResponseEntity.ok(service.allRatings());
    }

    @GetMapping("/aggregate")
    public ResponseEntity<List<Map<String, Object>>> aggregateAll(@RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        return ResponseEntity.ok(service.aggregateAll());
    }
}