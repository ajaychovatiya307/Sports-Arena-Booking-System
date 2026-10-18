package com.infy.sportsarena.booking.controller;

import com.infy.sportsarena.booking.dto.RatingDTO;
import com.infy.sportsarena.booking.service.RatingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class RatingControllerTest {
    private RatingService service;
    private RatingController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(RatingService.class);
        controller = new RatingController();
        var field = RatingController.class.getDeclaredField("service");
        field.setAccessible(true);
        field.set(controller, service);
    }

    @Test
    void delegatesRatingReadsAndSubmission() {
        RatingDTO dto = new RatingDTO();
        when(service.submit("U1", dto)).thenReturn(dto);
        when(service.myRatings("U1")).thenReturn(List.of(dto));
        when(service.aggregateForPackage(2L)).thenReturn(Map.of("count", 1));
        when(service.ratingsByPackage(2L)).thenReturn(List.of(dto));
        when(service.allRatings()).thenReturn(List.of(dto));
        when(service.aggregateAll()).thenReturn(List.of(Map.of("packageId", 2L)));

        assertThat(controller.submit("U1", dto).getBody()).isSameAs(dto);
        assertThat(controller.mine("U1").getBody()).containsExactly(dto);
        assertThat(controller.aggregateForPackage(2L).getBody()).containsEntry("count", 1);
        assertThat(controller.ratingsByPackage("ADMIN", 2L).getBody()).containsExactly(dto);
        assertThat(controller.allRatings("ADMIN").getBody()).containsExactly(dto);
        assertThat(controller.aggregateAll("ADMIN").getBody()).hasSize(1);
    }

    @Test
    void adminRatingEndpointsRejectNonAdmins() {
        assertThrows(RuntimeException.class, () -> controller.ratingsByPackage("CUSTOMER", 2L));
        assertThrows(RuntimeException.class, () -> controller.allRatings("CUSTOMER"));
        assertThrows(RuntimeException.class, () -> controller.aggregateAll("CUSTOMER"));
        verifyNoInteractions(service);
    }
}
