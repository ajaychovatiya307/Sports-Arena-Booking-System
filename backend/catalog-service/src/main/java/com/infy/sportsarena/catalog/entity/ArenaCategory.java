package com.infy.sportsarena.catalog.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "arena_category")
@Data
public class ArenaCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "category_code", unique = true, nullable = false)
    private String categoryCode;

    @Column(unique = true, nullable = false)
    private String name;

    private String description;
}
