package com.infy.sportsarena.catalog.repository;

import com.infy.sportsarena.catalog.entity.ArenaCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ArenaCategoryRepository extends JpaRepository<ArenaCategory, Long> {
    Optional<ArenaCategory> findByName(String name);
    boolean existsByName(String name);
}
