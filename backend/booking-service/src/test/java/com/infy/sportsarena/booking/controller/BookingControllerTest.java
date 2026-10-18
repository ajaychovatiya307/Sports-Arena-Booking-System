package com.infy.sportsarena.booking.controller;

import com.infy.sportsarena.booking.dto.BookingDTO;
import com.infy.sportsarena.booking.dto.DashboardSummaryDTO;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class BookingControllerTest {
    private BookingService service;
    private BookingController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(BookingService.class);
        controller = new BookingController();
        var field = BookingController.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(controller, service);
    }

    @Test
    void delegatesBookingOperations() {
        BookingDTO dto = new BookingDTO();
        BookingDTO result = new BookingDTO();
        when(service.create("U1", "CUSTOMER", dto)).thenReturn(result);
        when(service.getById("U1", "CUSTOMER", 1L)).thenReturn(result);
        when(service.search(eq("U1"), eq("ADMIN"), eq("ref"), eq("Jane"), eq(BookingStatus.PENDING),
                eq(LocalDate.of(2026, 10, 12)), eq(1), eq(5), eq("createdAt"), eq("desc")))
                .thenReturn(new PageImpl<>(List.of(result)));
        when(service.cancel("U1", "CUSTOMER", 1L)).thenReturn(result);
        when(service.updateStatus("ADMIN", 1L, BookingStatus.CONFIRMED)).thenReturn(result);
        when(service.dashboard("U1", "ADMIN")).thenReturn(new DashboardSummaryDTO());
        when(service.bookedSlots(2L, LocalDate.of(2026, 10, 12))).thenReturn(List.of(Map.of("timeSlot", "10:00", "status", "PENDING")));
        when(service.teamHasActive(3L)).thenReturn(true);
        when(service.packageHasActive(2L)).thenReturn(false);
        when(service.slotHasActiveBooking(2L, LocalDate.of(2026, 10, 12), "10:00")).thenReturn(true);

        assertThat(controller.create("U1", "CUSTOMER", dto).getBody()).isSameAs(result);
        assertThat(controller.get("U1", "CUSTOMER", 1L).getBody()).isSameAs(result);
        Page<BookingDTO> page = controller.search("U1", "ADMIN", "ref", "Jane", BookingStatus.PENDING,
                LocalDate.of(2026, 10, 12), 1, 5, "createdAt", "desc").getBody();
        assertThat(page.getContent()).containsExactly(result);
        assertThat(controller.cancel("U1", "CUSTOMER", 1L).getBody()).isSameAs(result);
        assertThat(controller.updateStatus("ADMIN", 1L, Map.of("status", "CONFIRMED")).getBody()).isSameAs(result);
        assertThat(controller.dashboard("U1", "ADMIN").getBody()).isNotNull();
        assertThat(controller.bookedSlots(2L, LocalDate.of(2026, 10, 12)).getBody()).hasSize(1);
        assertThat(controller.teamActive(3L).getBody()).isTrue();
        assertThat(controller.packageActive(2L).getBody()).isFalse();
        assertThat(controller.slotActive(2L, LocalDate.of(2026, 10, 12), "10:00").getBody()).isTrue();
    }

    @Test
    void updateStatusParsesRequestedStatus() {
        when(service.updateStatus("ADMIN", 1L, BookingStatus.CANCELLED)).thenReturn(new BookingDTO());
        ResponseEntity<BookingDTO> response = controller.updateStatus("ADMIN", 1L, Map.of("status", "CANCELLED"));
        assertThat(response.getBody()).isNotNull();
        verify(service).updateStatus("ADMIN", 1L, BookingStatus.CANCELLED);
    }
}
