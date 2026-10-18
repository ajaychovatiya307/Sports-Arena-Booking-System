package com.infy.sportsarena.catalog.controller;

import com.infy.sportsarena.catalog.dto.ArenaCategoryDTO;
import com.infy.sportsarena.catalog.service.ArenaCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ArenaCategoryControllerTest {
    private ArenaCategoryService service;
    private ArenaCategoryController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(ArenaCategoryService.class);
        controller = new ArenaCategoryController();
        var f = ArenaCategoryController.class.getDeclaredField("service");
        f.setAccessible(true); f.set(controller, service);
    }

    @Test
    void delegatesCategoryOperations() {
        ArenaCategoryDTO dto = new ArenaCategoryDTO();
        when(service.list()).thenReturn(List.of(dto));
        when(service.getById(1L)).thenReturn(dto);
        when(service.create(dto)).thenReturn(dto);
        when(service.update(1L, dto)).thenReturn(dto);
        when(service.count()).thenReturn(3L);

        assertThat(controller.list().getBody()).containsExactly(dto);
        assertThat(controller.get(1L).getBody()).isSameAs(dto);
        assertThat(controller.create("ADMIN", dto).getBody()).isSameAs(dto);
        assertThat(controller.update("ADMIN", 1L, dto).getBody()).isSameAs(dto);
        assertThat(controller.count().getBody()).containsEntry("totalCategories", 3L);
        assertThat(controller.delete("ADMIN", 1L).getBody()).containsEntry("message", "Category deleted");
        verify(service).delete(1L);
    }

    @Test
    void categoryMutationsRequireAdmin() {
        assertThrows(RuntimeException.class, () -> controller.create("CUSTOMER", new ArenaCategoryDTO()));
        assertThrows(RuntimeException.class, () -> controller.update("CUSTOMER", 1L, new ArenaCategoryDTO()));
        assertThrows(RuntimeException.class, () -> controller.delete("CUSTOMER", 1L));
        verifyNoInteractions(service);
    }
}
