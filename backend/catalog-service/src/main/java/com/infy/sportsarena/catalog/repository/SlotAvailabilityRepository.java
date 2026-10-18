package com.infy.sportsarena.catalog.repository;

import com.infy.sportsarena.catalog.entity.SlotAvailability;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface SlotAvailabilityRepository extends JpaRepository<SlotAvailability, Long> {
    List<SlotAvailability> findByPackageIdAndSlotDate(Long packageId, LocalDate slotDate);
    Optional<SlotAvailability> findByPackageIdAndSlotDateAndTimeSlot(Long packageId, LocalDate slotDate, String timeSlot);
    void deleteByPackageId(Long packageId);
}
