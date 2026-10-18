package com.infy.sportsarena.user.repository;

import com.infy.sportsarena.user.entity.SportType;
import com.infy.sportsarena.user.entity.Team;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TeamRepository extends JpaRepository<Team, Long> {
    List<Team> findByUserId(String userId);

    Page<Team> findByUserIdAndTeamNameContainingIgnoreCase(String userId, String q, Pageable p);

    Page<Team> findByUserIdAndSportType(String userId, SportType sportType, Pageable p);

    Page<Team> findByTeamNameContainingIgnoreCase(String q, Pageable p);

    // Admin sport-type filtering
    Page<Team> findBySportType(SportType sportType, Pageable p);

    Page<Team> findBySportTypeAndTeamNameContainingIgnoreCase(SportType sportType, String q, Pageable p);

    // Global team-name uniqueness (SRS: "Team names shall be unique.")
    boolean existsByTeamNameIgnoreCase(String teamName);

    boolean existsByTeamNameIgnoreCaseAndIdNot(String teamName, Long id);
}
