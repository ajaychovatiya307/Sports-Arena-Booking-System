package com.infy.sportsarena.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "arena_package")
@Data
public class ArenaPackage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_code", unique = true, nullable = false)
    private String packageCode;

    @Column(nullable = false)
    private String name;

    private String description;

    @Column(name = "price_per_hour", nullable = false)
    private BigDecimal pricePerHour;

    @Column(name = "duration_hours", nullable = false)
    private Integer durationHours;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "category_id", nullable = false)
    private ArenaCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AvailabilityStatus status;
}
