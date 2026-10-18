package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.ArenaPackageDTO;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import com.infy.sportsarena.catalog.service.ArenaPackageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ArenaPackageControllerTest {
    private ArenaPackageService service;
    private ArenaPackageController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(ArenaPackageService.class);
        controller = new ArenaPackageController();
        var f = ArenaPackageController.class.getDeclaredField("service");
        f.setAccessible(true); f.set(controller, service);
    }

    @Test
    void listingForcesActivePackagesForNonAdmins() {
        when(service.list("court", 2L, true, 1, 4, "name", "desc"))
                .thenReturn(new PageImpl<>(List.of(new ArenaPackageDTO())));
        assertThat(controller.list("CUSTOMER", "court", 2L, false, 1, 4, "name", "desc")
                .getBody().getTotalElements()).isEqualTo(1);
        verify(service).list("court", 2L, true, 1, 4, "name", "desc");
    }

    @Test
    void adminMayChooseActiveFilterAndReadPackage() {
        ArenaPackageDTO dto = new ArenaPackageDTO();
        when(service.list(null, null, false, 0, 10, "name", "asc")).thenReturn(new PageImpl<>(List.of()));
        when(service.getById(1L)).thenReturn(dto);
        assertThat(controller.list("ADMIN", null, null, false, 0, 10, "name", "asc").getBody()).isEmpty();
        assertThat(controller.get(1L).getBody()).isSameAs(dto);
    }

    @Test
    void mutationsRequireAdminAndDelegateToService() {
        ArenaPackageDTO dto = new ArenaPackageDTO();
        when(service.create(dto)).thenReturn(dto);
        when(service.update(1L, dto)).thenReturn(dto);
        when(service.countActive()).thenReturn(2L);
        assertThat(controller.create("ADMIN", dto).getBody()).isSameAs(dto);
        assertThat(controller.update("ADMIN", 1L, dto).getBody()).isSameAs(dto);
        assertThat(controller.delete("ADMIN", 1L).getBody()).containsEntry("message", "Package deleted");
        assertThat(controller.countActive().getBody()).containsEntry("totalActivePackages", 2L);
        assertThrows(RuntimeException.class, () -> controller.create("CUSTOMER", dto));
        assertThrows(RuntimeException.class, () -> controller.update("CUSTOMER", 1L, dto));
        assertThrows(RuntimeException.class, () -> controller.delete("CUSTOMER", 1L));
        verify(service).delete(1L);
    }
}
