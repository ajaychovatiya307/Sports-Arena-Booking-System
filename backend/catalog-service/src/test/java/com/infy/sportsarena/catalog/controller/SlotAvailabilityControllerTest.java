package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.SlotAvailabilityDTO;
import com.infy.sportsarena.catalog.service.SlotAvailabilityService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class SlotAvailabilityControllerTest {
    private SlotAvailabilityService service;
    private SlotAvailabilityController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(SlotAvailabilityService.class);
        controller = new SlotAvailabilityController();
        var f = SlotAvailabilityController.class.getDeclaredField("service");
        f.setAccessible(true); f.set(controller, service);
    }

    @Test
    void delegatesSlotOperations() {
        SlotAvailabilityDTO dto = new SlotAvailabilityDTO();
        LocalDate date = LocalDate.of(2026, 10, 12);
        when(service.upsert(dto)).thenReturn(dto);
        when(service.listForPackageAndDate(1L, date)).thenReturn(List.of(dto));
        when(service.isBlocked(1L, date, "10:00")).thenReturn(true);

        assertThat(controller.upsert("ADMIN", dto).getBody()).isSameAs(dto);
        assertThat(controller.list(1L, date).getBody()).containsExactly(dto);
        assertThat(controller.isBlocked(1L, date, "10:00").getBody()).containsEntry("blocked", true);
        assertThat(controller.delete("ADMIN", 1L).getBody()).containsEntry("message", "Slot deleted");
        verify(service).delete(1L);
    }

    @Test
    void slotMutationsRequireAdmin() {
        assertThrows(RuntimeException.class, () -> controller.upsert("CUSTOMER", new SlotAvailabilityDTO()));
        assertThrows(RuntimeException.class, () -> controller.delete("CUSTOMER", 1L));
        verifyNoInteractions(service);
    }
}
