package com.infy.sportsarena.booking.repository;

import com.infy.sportsarena.booking.entity.Booking;
import com.infy.sportsarena.booking.entity.BookingStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    long countByUserId(String userId);

    long countByUserIdAndStatus(String userId, BookingStatus status);

    long countByStatus(BookingStatus status);

    List<Booking> findTop5ByOrderByCreatedAtDesc();

    boolean existsByTeamIdAndStatusIn(Long teamId, List<BookingStatus> statuses);

    boolean existsByPackageIdAndStatusIn(Long packageId, List<BookingStatus> statuses);

    boolean existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
            Long packageId, LocalDate bookingDate, String timeSlot, List<BookingStatus> statuses);

    List<Booking> findByPackageIdAndBookingDateAndStatusIn(
            Long packageId, LocalDate bookingDate, List<BookingStatus> statuses);

    @Query("SELECT b FROM Booking b WHERE " +
           "(:userId IS NULL OR b.userId = :userId) AND " +
           "(:reference IS NULL OR LOWER(b.referenceNumber) LIKE LOWER(CONCAT('%', :reference, '%'))) AND " +
           "(:customerName IS NULL OR LOWER(b.customerName) LIKE LOWER(CONCAT('%', :customerName, '%'))) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:date IS NULL OR b.bookingDate = :date)")
    Page<Booking> search(@Param("userId") String userId,
                         @Param("reference") String reference,
                         @Param("customerName") String customerName,
                         @Param("status") BookingStatus status,
                         @Param("date") LocalDate date,
                         Pageable pageable);
}
