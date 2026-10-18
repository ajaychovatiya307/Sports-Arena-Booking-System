package com.infy.sportsarena.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class SlotAvailabilityDTO {
    private Long id;

    @NotNull(message = "Package id is required")
    private Long packageId;

    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;

    @NotBlank(message = "Time slot is required")
    private String timeSlot;

    private boolean blocked;

    private String reason;

    // SRS US11: when true, admin explicitly overrides the warning that the slot
    // is held by a non-cancelled booking. Not persisted — control flag only.
    private boolean override;
}
