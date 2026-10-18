package com.infy.sportsarena.booking.dto;

import com.infy.sportsarena.booking.entity.BookingStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class BookingDTOTest {
    @Test
    void accessorsEqualityHashCodeAndToStringCoverAllBookingFields() {
        BookingDTO a = sample();
        BookingDTO b = sample();
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
        assertThat(a).isNotEqualTo(changed(1)).isNotEqualTo(changed(2)).isNotEqualTo(changed(3))
                .isNotEqualTo(changed(4)).isNotEqualTo(changed(5)).isNotEqualTo(changed(6))
                .isNotEqualTo(changed(7)).isNotEqualTo(changed(8)).isNotEqualTo(changed(9))
                .isNotEqualTo(changed(10)).isNotEqualTo(changed(11)).isNotEqualTo(changed(12))
                .isNotEqualTo(changed(13)).isNotEqualTo(changed(14));
    }

    @Test
    void equalityHandlesNullValues() {
        BookingDTO a = new BookingDTO();
        BookingDTO b = new BookingDTO();
        assertThat(a).isEqualTo(b);
        assertThat(a.hashCode()).isEqualTo(b.hashCode());
        b.setReferenceNumber("ref");
        assertThat(a).isNotEqualTo(b);
    }

    private BookingDTO sample() {
        BookingDTO d = new BookingDTO();
        d.setId(1L); d.setReferenceNumber("ARN-10001"); d.setUserId("U1"); d.setCustomerName("Jane");
        d.setTeamId(2L); d.setTeamName("Team"); d.setPackageId(3L); d.setPackageName("Court");
        d.setBookingDate(LocalDate.of(2026, 10, 12)); d.setTimeSlot("10:00"); d.setSpecialRequest("None");
        d.setStatus(BookingStatus.PENDING); d.setTotalAmount(new BigDecimal("500"));
        d.setCreatedAt(LocalDateTime.of(2026, 10, 1, 10, 0));
        return d;
    }

    private BookingDTO changed(int field) {
        BookingDTO d = sample();
        switch (field) {
            case 1 -> d.setId(2L);
            case 2 -> d.setReferenceNumber("other");
            case 3 -> d.setUserId("U2");
            case 4 -> d.setCustomerName("Other");
            case 5 -> d.setTeamId(4L);
            case 6 -> d.setTeamName("Other");
            case 7 -> d.setPackageId(4L);
            case 8 -> d.setPackageName("Other");
            case 9 -> d.setBookingDate(LocalDate.of(2026, 10, 13));
            case 10 -> d.setTimeSlot("11:00");
            case 11 -> d.setSpecialRequest("Other");
            case 12 -> d.setStatus(BookingStatus.CONFIRMED);
            case 13 -> d.setTotalAmount(BigDecimal.ONE);
            case 14 -> d.setCreatedAt(LocalDateTime.MIN);
            default -> throw new IllegalArgumentException("Unknown field: " + field);
        }
        return d;
    }
}
