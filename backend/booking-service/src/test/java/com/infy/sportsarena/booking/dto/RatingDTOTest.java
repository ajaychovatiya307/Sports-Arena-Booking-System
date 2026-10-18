package com.infy.sportsarena.booking.dto;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RatingDTOTest {
    @Test
    void accessorsEqualityHashCodeAndToStringCoverRatingFields() {
        RatingDTO a = sample();
        RatingDTO b = sample();
        assertThat(a.getId()).isEqualTo(1L);
        assertThat(a.getBookingId()).isEqualTo(2L);
        assertThat(a.getReferenceNumber()).isEqualTo("ARN-10001");
        assertThat(a.getUserId()).isEqualTo("U1");
        assertThat(a.getPackageId()).isEqualTo(3L);
        assertThat(a.getStars()).isEqualTo(5);
        assertThat(a.getReview()).isEqualTo("Good");
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("ARN-10001", "Good");
        assertThat(a).isNotEqualTo(null).isNotEqualTo("rating");
        for (int i = 0; i < 7; i++) assertThat(a).isNotEqualTo(changed(i));
    }

    @Test
    void equalityHandlesNullFields() {
        RatingDTO a = new RatingDTO();
        RatingDTO b = new RatingDTO();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        b.setReview("review");
        assertThat(a).isNotEqualTo(b);
    }

    private RatingDTO sample() {
        RatingDTO d = new RatingDTO();
        d.setId(1L); d.setBookingId(2L); d.setReferenceNumber("ARN-10001"); d.setUserId("U1");
        d.setPackageId(3L); d.setStars(5); d.setReview("Good");
        return d;
    }

    private RatingDTO changed(int i) {
        RatingDTO d = sample();
        switch (i) {
            case 0 -> d.setId(9L);
            case 1 -> d.setBookingId(9L);
            case 2 -> d.setReferenceNumber("other");
            case 3 -> d.setUserId("U2");
            case 4 -> d.setPackageId(9L);
            case 5 -> d.setStars(4);
            case 6 -> d.setReview("Other");
            default -> throw new IllegalArgumentException();
        }
        return d;
    }
}
