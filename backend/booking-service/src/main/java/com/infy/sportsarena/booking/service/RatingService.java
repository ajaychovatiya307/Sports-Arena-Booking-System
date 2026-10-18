package com.infy.sportsarena.booking.service;

import com.infy.sportsarena.booking.dto.RatingDTO;
import com.infy.sportsarena.booking.entity.Booking;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.entity.Rating;
import com.infy.sportsarena.booking.repository.BookingRepository;
import com.infy.sportsarena.booking.repository.RatingRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class RatingService {

    @Autowired
    private RatingRepository ratingRepo;
    @Autowired
    private BookingRepository bookingRepo;
    @Autowired
    private ModelMapper mapper;

    public RatingDTO submit(String userId, RatingDTO dto) {
        Booking b = bookingRepo.findById(dto.getBookingId())
                .orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!b.getUserId().equals(userId))
            throw new RuntimeException("Unauthorized to rate this booking");
        if (b.getStatus() != BookingStatus.COMPLETED)
            throw new RuntimeException("Only COMPLETED bookings can be rated");
        if (ratingRepo.findByBookingId(b.getId()).isPresent())
            throw new RuntimeException("Rating already submitted for this booking");

        Rating r = new Rating();
        r.setBookingId(b.getId());
        r.setReferenceNumber(b.getReferenceNumber());
        r.setUserId(userId);
        r.setPackageId(b.getPackageId());
        r.setStars(dto.getStars());
        r.setReview(dto.getReview());
        return mapper.map(ratingRepo.save(r), RatingDTO.class);
    }

    public List<RatingDTO> myRatings(String userId) {
        return ratingRepo.findByUserId(userId).stream().map(r -> mapper.map(r, RatingDTO.class)).toList();
    }

    // SRS US12: Administrator shall be able to view individual ratings per arena
    // package.
    public List<RatingDTO> ratingsByPackage(Long packageId) {
        return ratingRepo.findByPackageId(packageId).stream().map(r -> mapper.map(r, RatingDTO.class)).toList();
    }

    // All individual ratings (admin) for drill-down reporting.
    public List<RatingDTO> allRatings() {
        return ratingRepo.findAll().stream().map(r -> mapper.map(r, RatingDTO.class)).toList();
    }

    public Map<String, Object> aggregateForPackage(Long packageId) {
        List<Rating> list = ratingRepo.findByPackageId(packageId);
        if (list.isEmpty())
            return Map.of("packageId", packageId, "count", 0, "average", 0.0);
        double avg = list.stream().mapToInt(Rating::getStars).average().orElse(0.0);
        return Map.of("packageId", packageId, "count", list.size(), "average", Math.round(avg * 10.0) / 10.0);
    }

    public List<Map<String, Object>> aggregateAll() {
        Map<Long, List<Rating>> grouped = new HashMap<>();
        for (Rating r : ratingRepo.findAll())
            grouped.computeIfAbsent(r.getPackageId(), k -> new ArrayList<>()).add(r);
        List<Map<String, Object>> out = new ArrayList<>();
        for (Map.Entry<Long, List<Rating>> e : grouped.entrySet()) {
            double avg = e.getValue().stream().mapToInt(Rating::getStars).average().orElse(0);
            out.add(Map.of("packageId", e.getKey(), "count", e.getValue().size(), "average",
                    Math.round(avg * 10.0) / 10.0));
        }
        return out;
    }
}