package com.infy.sportsarena.booking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "rating")
@Data
public class Rating {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "booking_id", unique = true, nullable = false)
    private Long bookingId;

    @Column(name = "reference_number", nullable = false)
    private String referenceNumber;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(nullable = false)
    private Integer stars;

    @Column(length = 1000)
    private String review;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() { if (createdAt == null) createdAt = LocalDateTime.now(); }
}
