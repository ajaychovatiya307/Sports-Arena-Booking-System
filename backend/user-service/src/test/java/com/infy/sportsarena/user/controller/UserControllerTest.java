package com.infy.sportsarena.user.controller;

import com.infy.sportsarena.user.dto.CustomerProfileDTO;
import com.infy.sportsarena.user.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class UserControllerTest {
    private UserService service;
    private UserController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(UserService.class);
        controller = new UserController();
        var f = UserController.class.getDeclaredField("userService");
        f.setAccessible(true); f.set(controller, service);
    }

    @Test
    void delegatesProfileOperations() {
        CustomerProfileDTO dto = new CustomerProfileDTO();
        when(service.createInternal(dto)).thenReturn(dto);
        when(service.getByUserId("U1")).thenReturn(dto);
        when(service.updateProfile("U1", dto)).thenReturn(dto);
        when(service.listAll("Jane", 0, 10, "userId", "asc")).thenReturn(new PageImpl<>(List.of(dto)));
        when(service.count()).thenReturn(4L);

        assertThat(controller.createInternal(dto).getBody()).isSameAs(dto);
        assertThat(controller.me("U1").getBody()).isSameAs(dto);
        assertThat(controller.updateMe("U1", dto).getBody()).isSameAs(dto);
        assertThat(controller.getByUserId("U1", "CUSTOMER", "U1").getBody()).isSameAs(dto);
        assertThat(controller.getByUserId("U9", "ADMIN", "U1").getBody()).isSameAs(dto);
        assertThat(controller.listAll("ADMIN", "Jane", 0, 10, "userId", "asc").getBody().getContent())
                .containsExactly(dto);
        assertThat(controller.stats("ADMIN").getBody()).containsEntry("totalCustomers", 4L);
    }

    @Test
    void restrictsProfileReadsAndAdminEndpoints() {
        assertThrows(RuntimeException.class, () -> controller.getByUserId("U2", "CUSTOMER", "U1"));
        assertThrows(RuntimeException.class, () -> controller.listAll("CUSTOMER", null, 0, 10, "userId", "asc"));
        assertThrows(RuntimeException.class, () -> controller.stats("CUSTOMER"));
        verifyNoInteractions(service);
    }
}
