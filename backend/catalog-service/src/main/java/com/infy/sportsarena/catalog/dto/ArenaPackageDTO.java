package com.infy.sportsarena.catalog.dto;

import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ArenaPackageDTO {
    private Long id;
    private String packageCode;

    @NotBlank(message = "Package name is required")
    private String name;

    private String description;

    @NotNull(message = "Price per hour is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be positive")
    private BigDecimal pricePerHour;

    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 hour")
    private Integer durationHours;

    @NotNull(message = "Category id is required")
    private Long categoryId;

    private String categoryName;

    private AvailabilityStatus status;
}
