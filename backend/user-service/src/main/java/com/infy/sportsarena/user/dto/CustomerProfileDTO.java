package com.infy.sportsarena.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CustomerProfileDTO {
    private Long id;
    private String userId;

    @NotBlank(message = "Full name is required")
    private String fullName;

    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email")
    private String email;

    private String contactNumber;
    private String address;
}
