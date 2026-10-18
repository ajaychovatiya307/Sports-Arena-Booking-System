package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.ArenaCategoryDTO;
import com.infy.sportsarena.catalog.service.ArenaCategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class ArenaCategoryController {

    @Autowired
    private ArenaCategoryService service;
    
    private void requireAdmin(String role) {
        if (!"ADMIN".equals(role))
            throw new RuntimeException("Unauthorized: admin only");
    }

    @GetMapping
    public ResponseEntity<List<ArenaCategoryDTO>> list() {
        return ResponseEntity.ok(service.list());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ArenaCategoryDTO> get(@PathVariable Long id) {
        return ResponseEntity.ok(service.getById(id));
    }

    @PostMapping
    public ResponseEntity<ArenaCategoryDTO> create(@RequestHeader("X-User-Role") String role,
            @Valid @RequestBody ArenaCategoryDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(service.create(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArenaCategoryDTO> update(@RequestHeader("X-User-Role") String role,
            @PathVariable Long id,
            @Valid @RequestBody ArenaCategoryDTO dto) {
        requireAdmin(role);
        return ResponseEntity.ok(service.update(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("X-User-Role") String role,
            @PathVariable Long id) {
        requireAdmin(role);
        service.delete(id);
        return ResponseEntity.ok(Map.of("message", "Category deleted"));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> count() {
        return ResponseEntity.ok(Map.of("totalCategories", service.count()));
    }
}
