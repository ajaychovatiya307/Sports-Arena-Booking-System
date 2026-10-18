package com.infy.sportsarena.booking.repository;

import com.infy.sportsarena.booking.entity.Rating;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RatingRepository extends JpaRepository<Rating, Long> {
    Optional<Rating> findByBookingId(Long bookingId);
    List<Rating> findByUserId(String userId);
    List<Rating> findByPackageId(Long packageId);
}
