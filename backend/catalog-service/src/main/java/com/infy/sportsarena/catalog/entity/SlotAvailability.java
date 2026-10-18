package com.infy.sportsarena.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDate;

@Entity
@Table(name = "slot_availability",
        uniqueConstraints = @UniqueConstraint(columnNames = {"package_id", "slot_date", "time_slot"}))
@Data
public class SlotAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "package_id", nullable = false)
    private Long packageId;

    @Column(name = "slot_date", nullable = false)
    private LocalDate slotDate;

    @Column(name = "time_slot", nullable = false)
    private String timeSlot;

    private boolean blocked;

    private String reason;
}
