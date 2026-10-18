package com.infy.sportsarena.booking.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RatingEntityTest {
    @Test
    void accessorsEqualityHashCodeAndToStringCoverAllFields() {
        Rating a = sample();
        Rating b = sample();
        assertThat(a.getId()).isEqualTo(1L);
        assertThat(a.getBookingId()).isEqualTo(2L);
        assertThat(a.getReferenceNumber()).isEqualTo("ARN-10001");
        assertThat(a.getUserId()).isEqualTo("U1");
        assertThat(a.getPackageId()).isEqualTo(3L);
        assertThat(a.getStars()).isEqualTo(5);
        assertThat(a.getReview()).isEqualTo("Great");
        assertThat(a.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 1, 1, 0, 0));
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("ARN-10001", "Great");
        assertThat(a).isNotEqualTo(null).isNotEqualTo("rating");
        for (int i = 0; i < 8; i++) assertThat(a).isNotEqualTo(changed(i));
    }

    @Test
    void prePersistInitializesMissingTimeAndPreservesExistingTime() {
        Rating fresh = new Rating();
        fresh.onCreate();
        assertThat(fresh.getCreatedAt()).isNotNull();

        Rating existing = new Rating();
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 0, 0);
        existing.setCreatedAt(timestamp);
        existing.onCreate();
        assertThat(existing.getCreatedAt()).isEqualTo(timestamp);
    }

    private Rating sample() {
        Rating r = new Rating();
        r.setId(1L); r.setBookingId(2L); r.setReferenceNumber("ARN-10001"); r.setUserId("U1");
        r.setPackageId(3L); r.setStars(5); r.setReview("Great");
        r.setCreatedAt(LocalDateTime.of(2026, 1, 1, 0, 0));
        return r;
    }

    private Rating changed(int i) {
        Rating r = sample();
        switch (i) {
            case 0 -> r.setId(9L);
            case 1 -> r.setBookingId(9L);
            case 2 -> r.setReferenceNumber("other");
            case 3 -> r.setUserId("U2");
            case 4 -> r.setPackageId(9L);
            case 5 -> r.setStars(4);
            case 6 -> r.setReview("Other");
            case 7 -> r.setCreatedAt(LocalDateTime.MIN);
            default -> throw new IllegalArgumentException();
        }
        return r;
    }
}
