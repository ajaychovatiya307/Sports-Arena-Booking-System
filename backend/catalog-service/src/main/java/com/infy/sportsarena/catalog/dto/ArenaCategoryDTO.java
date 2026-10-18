package com.infy.sportsarena.catalog.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ArenaCategoryDTO {
    private Long id;
    private String categoryCode;

    @NotBlank(message = "Category name is required")
    private String name;

    private String description;
}
