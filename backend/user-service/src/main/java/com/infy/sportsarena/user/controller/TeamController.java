package com.infy.sportsarena.user.controller;

import com.infy.sportsarena.user.dto.TeamDTO;
import com.infy.sportsarena.user.service.TeamService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/teams")
public class TeamController {

    @Autowired private TeamService teamService;

    @PostMapping
    public ResponseEntity<TeamDTO> create(@RequestHeader("X-User-Id") String userId,
                                          @Valid @RequestBody TeamDTO dto) {
        return ResponseEntity.ok(teamService.create(userId, dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TeamDTO> update(@RequestHeader("X-User-Id") String userId,
                                          @RequestHeader("X-User-Role") String role,
                                          @PathVariable Long id,
                                          @Valid @RequestBody TeamDTO dto) {
        return ResponseEntity.ok(teamService.update(userId, role, id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> delete(@RequestHeader("X-User-Id") String userId,
                                                     @RequestHeader("X-User-Role") String role,
                                                     @PathVariable Long id) {
        teamService.delete(userId, role, id);
        return ResponseEntity.ok(Map.of("message", "Team deleted"));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TeamDTO> get(@RequestHeader("X-User-Id") String userId,
                                       @RequestHeader("X-User-Role") String role,
                                       @PathVariable Long id) {
        return ResponseEntity.ok(teamService.getById(userId, role, id));
    }

    @GetMapping
    public ResponseEntity<Page<TeamDTO>> list(@RequestHeader("X-User-Id") String userId,
                                              @RequestHeader("X-User-Role") String role,
                                              @RequestParam(required = false) String q,
                                              @RequestParam(required = false) String sportType,
                                              @RequestParam(defaultValue = "0") int page,
                                              @RequestParam(defaultValue = "10") int size,
                                              @RequestParam(defaultValue = "id") String sortBy,
                                              @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(teamService.listForUser(userId, role, q, sportType, page, size, sortBy, direction));
    }

    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> myCount(@RequestHeader("X-User-Id") String userId) {
        return ResponseEntity.ok(Map.of("totalTeams", teamService.countByUser(userId)));
    }
}
