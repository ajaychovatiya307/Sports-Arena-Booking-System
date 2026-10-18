package com.infy.sportsarena.booking.service;

import com.infy.sportsarena.booking.dto.BookingDTO;
import com.infy.sportsarena.booking.dto.DashboardSummaryDTO;
import com.infy.sportsarena.booking.entity.Booking;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.repository.BookingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    @Mock
    private BookingRepository repository;
    @Mock
    private RestTemplate restTemplate;

    private BookingService service;

    @BeforeEach
    void setUp() {
        service = new BookingService();
        setField("repository", repository);
        setField("mapper", new ModelMapper());
        setField("restTemplate", restTemplate);
    }

    private void setField(String name, Object value) {
        try {
            var f = BookingService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private BookingDTO sampleDto() {
        BookingDTO dto = new BookingDTO();
        dto.setTeamId(1L);
        dto.setPackageId(2L);
        dto.setBookingDate(LocalDate.now().plusDays(1));
        dto.setTimeSlot("10:00-11:00");
        return dto;
    }

    @SuppressWarnings("unchecked")
    private void mockExchange(String urlContains, Map<String, Object> body) {
        when(restTemplate.exchange(contains(urlContains), eq(HttpMethod.GET), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));
    }

    @Test
    void create_throwsForAdminRole() {
        assertThrows(RuntimeException.class, () -> service.create("U1", "ADMIN", sampleDto()));
        verifyNoInteractions(repository);
    }

    @Test
    void create_throwsWhenBookingDateInPast() {
        BookingDTO dto = sampleDto();
        dto.setBookingDate(LocalDate.now().minusDays(1));
        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", dto));
    }

    @Test
    void create_throwsWhenTeamNotFound() {
        mockExchange("/api/teams/1", Map.of());
        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", sampleDto()));
    }

    @Test
    void create_throwsWhenTeamNotOwnedByUser() {
        mockExchange("/api/teams/1", Map.of("userId", "U2", "teamName", "Thunder"));
        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", sampleDto()));
    }

    @Test
    void create_throwsWhenPackageInactive() {
        mockExchange("/api/teams/1", Map.of("userId", "U1", "teamName", "Thunder"));
        mockExchange("/api/arena-packages/2", Map.of("status", "INACTIVE"));
        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", sampleDto()));
    }

    @Test
    void create_throwsWhenSlotBlocked() {
        mockExchange("/api/teams/1", Map.of("userId", "U1", "teamName", "Thunder"));
        mockExchange("/api/arena-packages/2", Map.of("status", "ACTIVE", "name", "Court", "pricePerHour", "500"));
        mockExchange("/api/slots/is-blocked", Map.of("blocked", true));

        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", sampleDto()));
    }

    @Test
    void create_throwsWhenSlotAlreadyBooked() {
        mockExchange("/api/teams/1", Map.of("userId", "U1", "teamName", "Thunder"));
        mockExchange("/api/arena-packages/2", Map.of("status", "ACTIVE", "name", "Court", "pricePerHour", "500"));
        mockExchange("/api/slots/is-blocked", Map.of("blocked", false));
        when(repository.existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
                eq(2L), any(LocalDate.class), eq("10:00-11:00"), anyList())).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create("U1", "CUSTOMER", sampleDto()));
    }

    @Test
    void create_succeedsAndGeneratesReferenceNumber() {
        mockExchange("/api/teams/1", Map.of("userId", "U1", "teamName", "Thunder"));
        mockExchange("/api/arena-packages/2", Map.of("status", "ACTIVE", "name", "Court", "pricePerHour", "500"));
        mockExchange("/api/slots/is-blocked", Map.of("blocked", false));
        mockExchange("/api/users/me", Map.of("fullName", "Jane"));
        when(repository.existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
                eq(2L), any(LocalDate.class), eq("10:00-11:00"), anyList())).thenReturn(false);
        when(repository.countByUserId("U1")).thenReturn(0L);
        when(repository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDTO result = service.create("U1", "CUSTOMER", sampleDto());

        assertThat(result.getReferenceNumber()).isEqualTo("ARN-10001");
        assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(new BigDecimal("500"));
    }

    @Test
    void updateStatus_throwsWhenNotAdmin() {
        assertThrows(RuntimeException.class, () -> service.updateStatus("CUSTOMER", 1L, BookingStatus.CONFIRMED));
    }

    @Test
    void updateStatus_throwsOnInvalidTransition() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.updateStatus("ADMIN", 1L, BookingStatus.CANCELLED));
    }

    @Test
    void updateStatus_allowsValidTransition() {
        Booking b = new Booking();
        b.setStatus(BookingStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(b));
        when(repository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDTO result = service.updateStatus("ADMIN", 1L, BookingStatus.CONFIRMED);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
    }

    @Test
    void cancel_throwsWhenNotOwnerOrAdmin() {
        Booking b = new Booking();
        b.setUserId("U2");
        b.setStatus(BookingStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.cancel("U1", "CUSTOMER", 1L));
    }

    @Test
    void cancel_throwsWhenNotPending() {
        Booking b = new Booking();
        b.setUserId("U1");
        b.setStatus(BookingStatus.CONFIRMED);
        when(repository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.cancel("U1", "CUSTOMER", 1L));
    }

    @Test
    void cancel_succeedsForOwnerWithPendingBooking() {
        Booking b = new Booking();
        b.setUserId("U1");
        b.setStatus(BookingStatus.PENDING);
        when(repository.findById(1L)).thenReturn(Optional.of(b));
        when(repository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

        BookingDTO result = service.cancel("U1", "CUSTOMER", 1L);

        assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
    }

    @Test
    void getById_throwsWhenUnauthorized() {
        Booking b = new Booking();
        b.setUserId("U2");
        when(repository.findById(1L)).thenReturn(Optional.of(b));

        assertThrows(RuntimeException.class, () -> service.getById("U1", "CUSTOMER", 1L));
    }

    @Test
    void search_scopesToUserForNonAdmin() {
        Page<Booking> page = new PageImpl<>(List.of(new Booking()));
        when(repository.search(eq("U1"), isNull(), isNull(), isNull(), isNull(), any(Pageable.class)))
                .thenReturn(page);

        Page<BookingDTO> result = service.search("U1", "CUSTOMER", null, null, null, null, 0, 10, "bookingDate", "desc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void dashboard_customerSeesOwnCounts() {
        mockExchange("/api/teams/count", Map.of("totalTeams", 3));
        when(repository.countByUserId("U1")).thenReturn(5L);
        when(repository.countByUserIdAndStatus(eq("U1"), any(BookingStatus.class))).thenReturn(1L);

        DashboardSummaryDTO result = service.dashboard("U1", "CUSTOMER");

        assertThat(result.getTotalTeams()).isEqualTo(3);
        assertThat(result.getTotalBookings()).isEqualTo(5L);
        assertThat(result.getBookingsByStatus()).hasSize(BookingStatus.values().length);
    }

    @Test
    void dashboard_adminSeesSystemWideCounts() {
        mockExchange("/api/users/stats", Map.of("totalCustomers", 10));
        mockExchange("/api/categories/count", Map.of("totalCategories", 4));
        mockExchange("/api/arena-packages/count", Map.of("totalActivePackages", 6));
        when(repository.count()).thenReturn(20L);
        when(repository.countByStatus(any(BookingStatus.class))).thenReturn(2L);
        when(repository.findTop5ByOrderByCreatedAtDesc()).thenReturn(List.of());

        DashboardSummaryDTO result = service.dashboard("A1", "ADMIN");

        assertThat(result.getTotalCustomers()).isEqualTo(10);
        assertThat(result.getTotalBookings()).isEqualTo(20L);
    }

    @Test
    void teamHasActive_delegatesToRepository() {
        when(repository.existsByTeamIdAndStatusIn(eq(1L), anyList())).thenReturn(true);
        assertThat(service.teamHasActive(1L)).isTrue();
    }

    @Test
    void packageHasActive_delegatesToRepository() {
        when(repository.existsByPackageIdAndStatusIn(eq(2L), anyList())).thenReturn(false);
        assertThat(service.packageHasActive(2L)).isFalse();
    }

    @Test
    void slotHasActiveBooking_delegatesToRepository() {
        when(repository.existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
                eq(2L), any(LocalDate.class), eq("10:00-11:00"), anyList())).thenReturn(true);

        assertThat(service.slotHasActiveBooking(2L, LocalDate.now(), "10:00-11:00")).isTrue();
    }

    @Test
    void bookedSlots_mapsTimeSlotAndStatus() {
        Booking b = new Booking();
        b.setTimeSlot("09:00-10:00");
        b.setStatus(BookingStatus.CONFIRMED);
        when(repository.findByPackageIdAndBookingDateAndStatusIn(eq(2L), any(LocalDate.class), anyList()))
                .thenReturn(List.of(b));

        List<Map<String, String>> result = service.bookedSlots(2L, LocalDate.now());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).get("timeSlot")).isEqualTo("09:00-10:00");
        assertThat(result.get(0).get("status")).isEqualTo("CONFIRMED");
    }
}
