package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.SlotAvailabilityDTO;
import com.infy.sportsarena.catalog.service.SlotAvailabilityService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/slots")
public class SlotAvailabilityController {

    @Autowired private SlotAvailabilityService service;

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
    }

    @PostMapping
    public ResponseEntity<SlotAvailabilityDTO> upsert(@RequestHeader("X-User-Role") String role,
                                                     @Valid @RequestBody SlotAvailabilityDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(service.upsert(dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("X-User-Role") String role,
                                                     @PathVariable Long id) {
        requireAdmin(role);
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Slot deleted"));
    }

    @GetMapping
    public ResponseEntity<List<SlotAvailabilityDTO>> list(
            @RequestParam Long packageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.listForPackageAndDate(packageId, date));
    }

    @GetMapping("/is-blocked")
    public ResponseEntity<Map<String, Boolean>> isBlocked(
            @RequestParam Long packageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String timeSlot) {
        return ResponseEntity.ok(Map.of("blocked", service.isBlocked(packageId, date, timeSlot)));
    }
}
