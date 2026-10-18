package com.infy.sportsarena.catalog.repository;

import com.infy.sportsarena.catalog.entity.ArenaPackage;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ArenaPackageRepository extends JpaRepository<ArenaPackage, Long> {
    Page<ArenaPackage> findByStatus(AvailabilityStatus status, Pageable pageable);
    Page<ArenaPackage> findByStatusAndCategoryId(AvailabilityStatus status, Long categoryId, Pageable pageable);
    Page<ArenaPackage> findByStatusAndNameContainingIgnoreCase(AvailabilityStatus status, String name, Pageable pageable);
    Page<ArenaPackage> findByNameContainingIgnoreCase(String name, Pageable pageable);
    long countByCategoryIdAndStatus(Long categoryId, AvailabilityStatus status);
    long countByStatus(AvailabilityStatus status);
    List<ArenaPackage> findByCategoryId(Long categoryId);
}
