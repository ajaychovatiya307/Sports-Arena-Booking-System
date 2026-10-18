package com.infy.sportsarena.booking.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RatingDTO {
    private Long id;

    @NotNull(message = "Booking id is required")
    private Long bookingId;

    private String referenceNumber;
    private String userId;
    private Long packageId;

    @NotNull(message = "Stars is required")
    @Min(value = 1, message = "Minimum 1 star")
    @Max(value = 5, message = "Maximum 5 stars")
    private Integer stars;

    @Size(max = 1000)
    private String review;
}
