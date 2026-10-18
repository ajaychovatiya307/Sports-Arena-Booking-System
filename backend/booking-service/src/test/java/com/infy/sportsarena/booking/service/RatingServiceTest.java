package com.infy.sportsarena.booking.service;

import com.infy.sportsarena.booking.dto.RatingDTO;
import com.infy.sportsarena.booking.entity.Booking;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.entity.Rating;
import com.infy.sportsarena.booking.repository.BookingRepository;
import com.infy.sportsarena.booking.repository.RatingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RatingServiceTest {

    @Mock
    private RatingRepository ratingRepo;
    @Mock
    private BookingRepository bookingRepo;

    private RatingService service;

    @BeforeEach
    void setUp() {
        service = new RatingService();
        setField("ratingRepo", ratingRepo);
        setField("bookingRepo", bookingRepo);
        setField("mapper", new ModelMapper());
    }

    private void setField(String name, Object value) {
        try {
            var f = RatingService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private RatingDTO sampleDto() {
        RatingDTO dto = new RatingDTO();
        dto.setBookingId(1L);
        dto.setStars(5);
        dto.setReview("Great!");
        return dto;
    }

    @Test
    void submit_throwsWhenBookingNotFound() {
        when(bookingRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.submit("U1", sampleDto()));
    }

    @Test
    void submit_throwsWhenNotOwner() {
        Booking b = new Booking();
        b.setUserId("U2");
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.submit("U1", sampleDto()));
    }

    @Test
    void submit_throwsWhenBookingNotCompleted() {
        Booking b = new Booking();
        b.setUserId("U1");
        b.setStatus(BookingStatus.CONFIRMED);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.submit("U1", sampleDto()));
    }

    @Test
    void submit_throwsWhenAlreadyRated() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUserId("U1");
        b.setStatus(BookingStatus.COMPLETED);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(ratingRepo.findByBookingId(1L)).thenReturn(Optional.of(new Rating()));

        assertThrows(RuntimeException.class, () -> service.submit("U1", sampleDto()));
    }

    @Test
    void submit_savesRatingWhenValid() {
        Booking b = new Booking();
        b.setId(1L);
        b.setUserId("U1");
        b.setStatus(BookingStatus.COMPLETED);
        b.setReferenceNumber("ARN-10001");
        b.setPackageId(2L);
        when(bookingRepo.findById(1L)).thenReturn(Optional.of(b));
        when(ratingRepo.findByBookingId(1L)).thenReturn(Optional.empty());
        when(ratingRepo.save(any(Rating.class))).thenAnswer(inv -> inv.getArgument(0));

        RatingDTO result = service.submit("U1", sampleDto());

        assertThat(result.getStars()).isEqualTo(5);
        assertThat(result.getReview()).isEqualTo("Great!");
    }

    @Test
    void myRatings_returnsMappedList() {
        Rating r = new Rating();
        r.setStars(4);
        when(ratingRepo.findByUserId("U1")).thenReturn(List.of(r));

        List<RatingDTO> result = service.myRatings("U1");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getStars()).isEqualTo(4);
    }

    @Test
    void aggregateForPackage_returnsZeroWhenNoRatings() {
        when(ratingRepo.findByPackageId(2L)).thenReturn(List.of());

        Map<String, Object> result = service.aggregateForPackage(2L);

        assertThat(result.get("count")).isEqualTo(0);
        assertThat(result.get("average")).isEqualTo(0.0);
    }

    @Test
    void aggregateForPackage_computesAverage() {
        Rating r1 = new Rating(); r1.setStars(4);
        Rating r2 = new Rating(); r2.setStars(5);
        when(ratingRepo.findByPackageId(2L)).thenReturn(List.of(r1, r2));

        Map<String, Object> result = service.aggregateForPackage(2L);

        assertThat(result.get("count")).isEqualTo(2);
        assertThat(result.get("average")).isEqualTo(4.5);
    }

    @Test
    void aggregateAll_groupsByPackage() {
        Rating r1 = new Rating(); r1.setPackageId(1L); r1.setStars(3);
        Rating r2 = new Rating(); r2.setPackageId(2L); r2.setStars(5);
        when(ratingRepo.findAll()).thenReturn(List.of(r1, r2));

        List<Map<String, Object>> result = service.aggregateAll();

        assertThat(result).hasSize(2);
    }
}
