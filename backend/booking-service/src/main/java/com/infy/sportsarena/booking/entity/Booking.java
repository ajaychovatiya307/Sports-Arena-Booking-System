package com.infy.sportsarena.booking.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "booking")
@Data
public class Booking {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_number", unique = true, nullable = false)
    private String referenceNumber;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @Column(name = "customer_name")
    private String customerName;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @Column(name = "team_name")
    private String teamName;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "package_name")
    private String packageName;

    @Column(name = "booking_date", nullable = false)
    private LocalDate bookingDate;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    @Column(name = "special_request", length = 1000)
    private String specialRequest;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BookingStatus status;

    @Column(name = "total_amount")
    private BigDecimal totalAmount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        if (status == null) status = BookingStatus.PENDING;
    }
}
