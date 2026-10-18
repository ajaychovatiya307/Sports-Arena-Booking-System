package com.infy.sportsarena.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "team")
@Data
public class Team {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "team_name", nullable = false)
    private String teamName;

    @Enumerated(EnumType.STRING)
    @Column(name = "sport_type", nullable = false)
    private SportType sportType;

    @Column(name = "number_of_players", nullable = false)
    private Integer numberOfPlayers;

    @Column(name = "coach_name")
    private String coachName;

    @Column(name = "founded_year", nullable = false)
    private Integer foundedYear;
}
