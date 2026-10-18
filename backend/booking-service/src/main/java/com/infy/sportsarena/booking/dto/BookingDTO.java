package com.infy.sportsarena.booking.dto;

import com.infy.sportsarena.booking.entity.BookingStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingDTO {
    private Long id;
    private String referenceNumber;
    private String userId;
    private String customerName;

    @NotNull(message = "Team id is required")
    private Long teamId;
    private String teamName;

    @NotNull(message = "Package id is required")
    private Long packageId;
    private String packageName;

    @NotNull(message = "Booking date is required")
    private LocalDate bookingDate;

    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    private String specialRequest;
    private BookingStatus status;
    private BigDecimal totalAmount;
    private LocalDateTime createdAt;
}
