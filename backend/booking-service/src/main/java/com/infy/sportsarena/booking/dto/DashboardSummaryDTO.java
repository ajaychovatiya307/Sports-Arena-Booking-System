package com.infy.sportsarena.booking.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class DashboardSummaryDTO {
    private long totalCustomers;
    private long totalCategories;
    private long totalPackages;
    private long totalTeams;
    private long totalBookings;
    private long activeBookings;
    private long completedBookings;
    private Map<String, Long> bookingsByStatus;
    private List<BookingDTO> recentBookings;
}
