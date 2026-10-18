package com.infy.sportsarena.user.dto;

import com.infy.sportsarena.user.entity.SportType;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TeamDTO {
    private Long id;
    private String userId;

    @NotBlank(message = "Team name is required")
    @Size(max = 150)
    private String teamName;

    @NotNull(message = "Sport type is required")
    private SportType sportType;

    @NotNull(message = "Number of players is required")
    @Min(value = 1, message = "Must have at least 1 player")
    @Max(value = 30, message = "Cannot exceed 30 players")
    private Integer numberOfPlayers;

    private String coachName;

    @NotNull(message = "Founded year is required")
    @Min(value = 1900, message = "Founded year must be >= 1900")
    private Integer foundedYear;
}
