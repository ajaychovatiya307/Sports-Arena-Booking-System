package com.infy.sportsarena.user.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "customer_profile")
@Data
public class CustomerProfile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", unique = true, nullable = false)
    private String userId;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(name = "contact_number")
    private String contactNumber;

    private String address;
}
