package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.ArenaPackageDTO;
import com.infy.sportsarena.catalog.service.ArenaPackageService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/arena-packages")
public class ArenaPackageController {

    @Autowired private ArenaPackageService service;

    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
    }

    @GetMapping
    public ResponseEntity<Page<ArenaPackageDTO>> list(
            @RequestHeader(value = "X-User-Role", required = false) String role,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) Boolean activeOnly,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        // SRS: Customers shall have read-only access to ACTIVE arena packages only.
        // Only administrators may list inactive packages; everyone else is forced to active.
        Boolean effectiveActiveOnly = "ADMIN".equals(role) ? activeOnly : Boolean.TRUE;
        return ResponseEntity.ok(service.list(q, categoryId, effectiveActiveOnly, page, size, sortBy, direction));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArenaPackageDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ArenaPackageDTO> create(@RequestHeader("X-User-Role") String role,
                                                  @Valid @RequestBody ArenaPackageDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArenaPackageDTO> update(@RequestHeader("X-User-Role") String role,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody ArenaPackageDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("X-User-Role") String role,
                                                      @PathVariable Long id) {
        requireAdmin(role);
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Package deleted"));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> countActive() {
        return ResponseEntity.ok(Map.of("totalActivePackages", service.countActive()));
    }
}
