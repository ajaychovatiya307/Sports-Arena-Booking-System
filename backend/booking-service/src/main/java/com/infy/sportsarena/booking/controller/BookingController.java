package com.infy.sportsarena.booking.controller;

import com.infy.sportsarena.booking.dto.BookingDTO;
import com.infy.sportsarena.booking.dto.DashboardSummaryDTO;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    @Autowired private BookingService service;

    @PostMapping
    public ResponseEntity<BookingDTO> create(@RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String role,
                                             @Valid @RequestBody BookingDTO dto) {
        return ResponseEntity.ok(service.create(userId, role, dto));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingDTO> get(@RequestHeader("X-User-Id") String userId,
                                          @RequestHeader("X-User-Role") String role,
                                          @PathVariable Long id) {
        return ResponseEntity.ok(service.getById(userId, role, id));
    }

    @GetMapping
    public ResponseEntity<Page<BookingDTO>> search(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-User-Role") String role,
            @RequestParam(required = false) String reference,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) BookingStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction) {
        return ResponseEntity.ok(service.search(userId, role, reference, 
            customerName, status, date, page, size, sortBy, direction));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<BookingDTO> updateStatus(@RequestHeader("X-User-Role") String role,
                                                   @PathVariable Long id,
                                                   @RequestBody Map<String, String> body) {
        BookingStatus newStatus = BookingStatus.valueOf(body.get("status"));
        return ResponseEntity.ok(service.updateStatus(role, id, newStatus));
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<BookingDTO> cancel(@RequestHeader("X-User-Id") String userId,
                                             @RequestHeader("X-User-Role") String role,
                                             @PathVariable Long id) {
        return ResponseEntity.ok(service.cancel(userId, role, id));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummaryDTO> dashboard(@RequestHeader("X-User-Id") String userId,
                                                         @RequestHeader("X-User-Role") String role) {
        return ResponseEntity.ok(service.dashboard(userId, role));
    }

    // Slots already held by a customer booking for a package/date (timeSlot + status only).
    // Used by the admin Slot Availability page and the customer Booking form to flag
    // slots that are unavailable because they are booked (not blocked by an admin).
    @GetMapping("/booked-slots")
    public ResponseEntity<java.util.List<Map<String, String>>> bookedSlots(
            @RequestParam Long packageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(service.bookedSlots(packageId, date));
    }

    // Internal endpoints used by other services (no auth headers required)
    @GetMapping("/internal/team/{teamId}/active-exists")
    public ResponseEntity<Boolean> teamActive(@PathVariable Long teamId) {
        return ResponseEntity.ok(service.teamHasActive(teamId));
    }

    @GetMapping("/internal/package/{packageId}/active-exists")
    public ResponseEntity<Boolean> packageActive(@PathVariable Long packageId) {
        return ResponseEntity.ok(service.packageHasActive(packageId));
    }

    // SRS US11: used by catalog-service to warn before blocking a slot that is held by a non-cancelled booking.
    @GetMapping("/internal/slot/active-exists")
    public ResponseEntity<Boolean> slotActive(
            @RequestParam Long packageId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam String timeSlot) {
        return ResponseEntity.ok(service.slotHasActiveBooking(packageId, date, timeSlot));
    }
}