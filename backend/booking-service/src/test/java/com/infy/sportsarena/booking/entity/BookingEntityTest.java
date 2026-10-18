package com.infy.sportsarena.booking.entity;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingEntityTest {
    @Test
    void accessorsEqualityHashCodeAndToStringCoverAllFields() {
        Booking a = sample();
        Booking b = sample();
        assertThat(a.getId()).isEqualTo(1L);
        assertThat(a.getReferenceNumber()).isEqualTo("ARN-10001");
        assertThat(a.getUserId()).isEqualTo("U1");
        assertThat(a.getCustomerName()).isEqualTo("Jane");
        assertThat(a.getTeamId()).isEqualTo(2L);
        assertThat(a.getTeamName()).isEqualTo("Team");
        assertThat(a.getPackageId()).isEqualTo(3L);
        assertThat(a.getPackageName()).isEqualTo("Court");
        assertThat(a.getBookingDate()).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(a.getTimeSlot()).isEqualTo("10:00");
        assertThat(a.getSpecialRequest()).isEqualTo("None");
        assertThat(a.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(a.getTotalAmount()).isEqualByComparingTo("500");
        assertThat(a.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 10, 1, 10, 0));
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("ARN-10001", "Jane", "PENDING");
        assertThat(a).isNotEqualTo(null).isNotEqualTo("booking");
        for (int i = 0; i < 14; i++) assertThat(a).isNotEqualTo(changed(i));
    }

    @Test
    void prePersistInitializesMissingCreationTimeAndStatus() {
        Booking booking = new Booking();
        booking.onCreate();
        assertThat(booking.getCreatedAt()).isNotNull();
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.PENDING);
    }

    @Test
    void prePersistPreservesExistingCreationTimeAndStatus() {
        Booking booking = new Booking();
        LocalDateTime created = LocalDateTime.of(2026, 1, 1, 0, 0);
        booking.setCreatedAt(created);
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.onCreate();
        assertThat(booking.getCreatedAt()).isEqualTo(created);
        assertThat(booking.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    private Booking sample() {
        Booking b = new Booking();
        b.setId(1L); b.setReferenceNumber("ARN-10001"); b.setUserId("U1"); b.setCustomerName("Jane");
        b.setTeamId(2L); b.setTeamName("Team"); b.setPackageId(3L); b.setPackageName("Court");
        b.setBookingDate(LocalDate.of(2026, 10, 12)); b.setTimeSlot("10:00"); b.setSpecialRequest("None");
        b.setStatus(BookingStatus.PENDING); b.setTotalAmount(new BigDecimal("500"));
        b.setCreatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        return b;
    }

    private Booking changed(int i) {
        Booking b = sample();
        switch (i) {
            case 0 -> b.setId(9L);
            case 1 -> b.setReferenceNumber("other");
            case 2 -> b.setUserId("U2");
            case 3 -> b.setCustomerName("Other");
            case 4 -> b.setTeamId(9L);
            case 5 -> b.setTeamName("Other");
            case 6 -> b.setPackageId(9L);
            case 7 -> b.setPackageName("Other");
            case 8 -> b.setBookingDate(LocalDate.MIN);
            case 9 -> b.setTimeSlot("11:00");
            case 10 -> b.setSpecialRequest("Other");
            case 11 -> b.setStatus(BookingStatus.CONFIRMED);
            case 12 -> b.setTotalAmount(BigDecimal.ONE);
            case 13 -> b.setCreatedAt(LocalDateTime.MIN);
            default -> throw new IllegalArgumentException();
        }
        return b;
    }
}
