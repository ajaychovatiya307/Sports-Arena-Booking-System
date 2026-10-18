package com.infy.sportsarena.booking.dto;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class DashboardSummaryDTOTest {
    @Test
    void accessorsEqualityHashCodeAndToStringCoverDashboardFields() {
        DashboardSummaryDTO a = sample();
        DashboardSummaryDTO b = sample();
        assertThat(a.getTotalCustomers()).isEqualTo(1);
        assertThat(a.getTotalCategories()).isEqualTo(2);
        assertThat(a.getTotalPackages()).isEqualTo(3);
        assertThat(a.getTotalTeams()).isEqualTo(4);
        assertThat(a.getTotalBookings()).isEqualTo(5);
        assertThat(a.getActiveBookings()).isEqualTo(6);
        assertThat(a.getCompletedBookings()).isEqualTo(7);
        assertThat(a.getBookingsByStatus()).containsEntry("PENDING", 2L);
        assertThat(a.getRecentBookings()).hasSize(1);
        assertThat(a).isEqualTo(b).hasSameHashCodeAs(b);
        assertThat(a.toString()).contains("PENDING");
        assertThat(a).isNotEqualTo(null).isNotEqualTo("dashboard");
        for (int i = 0; i < 9; i++) assertThat(a).isNotEqualTo(changed(i));
    }

    private DashboardSummaryDTO sample() {
        DashboardSummaryDTO d = new DashboardSummaryDTO();
        d.setTotalCustomers(1); d.setTotalCategories(2); d.setTotalPackages(3); d.setTotalTeams(4);
        d.setTotalBookings(5); d.setActiveBookings(6); d.setCompletedBookings(7);
        d.setBookingsByStatus(Map.of("PENDING", 2L)); d.setRecentBookings(List.of(new BookingDTO()));
        return d;
    }

    private DashboardSummaryDTO changed(int i) {
        DashboardSummaryDTO d = sample();
        switch (i) {
            case 0 -> d.setTotalCustomers(10);
            case 1 -> d.setTotalCategories(10);
            case 2 -> d.setTotalPackages(10);
            case 3 -> d.setTotalTeams(10);
            case 4 -> d.setTotalBookings(10);
            case 5 -> d.setActiveBookings(10);
            case 6 -> d.setCompletedBookings(10);
            case 7 -> d.setBookingsByStatus(Map.of("COMPLETED", 1L));
            case 8 -> d.setRecentBookings(List.of());
            default -> throw new IllegalArgumentException();
        }
        return d;
    }
}
