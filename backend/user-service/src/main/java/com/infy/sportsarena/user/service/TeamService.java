package com.infy.sportsarena.user.service;

import com.infy.sportsarena.user.dto.TeamDTO;
import com.infy.sportsarena.user.entity.SportType;
import com.infy.sportsarena.user.entity.Team;
import com.infy.sportsarena.user.repository.TeamRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Service
public class TeamService {

    @Autowired private TeamRepository repository;
    @Autowired private ModelMapper mapper;
    @Autowired private RestTemplate restTemplate;

    public TeamDTO create(String userId, TeamDTO dto) {
        if (dto.getTeamName() != null && repository.existsByTeamNameIgnoreCase(dto.getTeamName().trim())) {
            throw new RuntimeException("Team name '" + dto.getTeamName() + "' already exists");
        }
        Team t = new Team();
        t.setUserId(userId);
        t.setTeamName(dto.getTeamName());
        t.setSportType(dto.getSportType());
        t.setNumberOfPlayers(dto.getNumberOfPlayers());
        t.setCoachName(dto.getCoachName());
        t.setFoundedYear(dto.getFoundedYear());
        return mapper.map(repository.save(t), TeamDTO.class);
    }

    public TeamDTO update(String userId, String role, Long id, TeamDTO dto) {
        // SRS: Administrators shall have read-only access to team information.
        if ("ADMIN".equals(role)) {
            throw new RuntimeException("Administrators have read-only access to teams");
        }
        Team t = repository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!t.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to update this team");
        }
        if (dto.getTeamName() != null && repository.existsByTeamNameIgnoreCaseAndIdNot(dto.getTeamName().trim(), id)) {
            throw new RuntimeException("Team name '" + dto.getTeamName() + "' already exists");
        }
        t.setTeamName(dto.getTeamName());
        t.setSportType(dto.getSportType());
        t.setNumberOfPlayers(dto.getNumberOfPlayers());
        t.setCoachName(dto.getCoachName());
        t.setFoundedYear(dto.getFoundedYear());
        return mapper.map(repository.save(t), TeamDTO.class);
    }

    public void delete(String userId, String role, Long id) {
        // SRS: Administrators shall have read-only access to team information.
        if ("ADMIN".equals(role)) {
            throw new RuntimeException("Administrators have read-only access to teams");
        }
        Team t = repository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!t.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to delete this team");
        }
        if (hasActiveBookings(id)) {
            throw new RuntimeException("Cannot delete team with active bookings");
        }
        repository.delete(t);
    }

    public TeamDTO getById(String userId, String role, Long id) {
        Team t = repository.findById(id).orElseThrow(() -> new RuntimeException("Team not found"));
        if (!"ADMIN".equals(role) && !t.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this team");
        }
        return mapper.map(t, TeamDTO.class);
    }

    public Page<TeamDTO> listForUser(String userId, String role, String q, String sportType,
                                     int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Team> result;
        if ("ADMIN".equals(role)) {
            boolean hasQ = q != null && !q.isBlank();
            boolean hasSport = sportType != null && !sportType.isBlank();
            if (hasSport && hasQ) {
                result = repository.findBySportTypeAndTeamNameContainingIgnoreCase(SportType.valueOf(sportType), q, pageable);
            } else if (hasSport) {
                result = repository.findBySportType(SportType.valueOf(sportType), pageable);
            } else if (hasQ) {
                result = repository.findByTeamNameContainingIgnoreCase(q, pageable);
            } else {
                result = repository.findAll(pageable);
            }
        } else if (sportType != null && !sportType.isBlank()) {
            result = repository.findByUserIdAndSportType(userId, SportType.valueOf(sportType), pageable);
        } else if (q != null && !q.isBlank()) {
            result = repository.findByUserIdAndTeamNameContainingIgnoreCase(userId, q, pageable);
        } else {
            List<Team> all = repository.findByUserId(userId);
            // manual sort/page — simple path
            int from = Math.min(page * size, all.size());
            int to = Math.min(from + size, all.size());
            List<Team> slice = all.subList(from, to);
            return new org.springframework.data.domain.PageImpl<>(
                    slice.stream().map(t -> mapper.map(t, TeamDTO.class)).toList(),
                    pageable, all.size());
        }
        return result.map(t -> mapper.map(t, TeamDTO.class));
    }

    public long countByUser(String userId) {
        return repository.findByUserId(userId).size();
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "hasActiveBookingsFallback")
    public boolean hasActiveBookings(Long teamId) {
        Boolean result = restTemplate.getForObject(
                "http://booking-service/api/bookings/internal/team/" + teamId + "/active-exists",
                Boolean.class);
        return Boolean.TRUE.equals(result);
    }

    @SuppressWarnings("unused")
    private boolean hasActiveBookingsFallback(Long teamId, Throwable ex) {
        // Fail-safe: refuse deletion if booking-service is unreachable
        return true;
    }
}