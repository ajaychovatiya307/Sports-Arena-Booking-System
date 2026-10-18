package com.infy.sportsarena.user.controller;

import com.infy.sportsarena.user.dto.TeamDTO;
import com.infy.sportsarena.user.service.TeamService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class TeamControllerTest {
    private TeamService service;
    private TeamController controller;

    @BeforeEach
    void setUp() throws Exception {
        service = mock(TeamService.class);
        controller = new TeamController();
        var f = TeamController.class.getDeclaredField("teamService");
        f.setAccessible(true); f.set(controller, service);
    }

    @Test
    void delegatesTeamOperations() {
        TeamDTO dto = new TeamDTO();
        when(service.create("U1", dto)).thenReturn(dto);
        when(service.update("U1", "CUSTOMER", 1L, dto)).thenReturn(dto);
        when(service.getById("U1", "CUSTOMER", 1L)).thenReturn(dto);
        when(service.listForUser("U1", "CUSTOMER", "Thunder", "FOOTBALL", 0, 10, "id", "asc"))
                .thenReturn(new PageImpl<>(List.of(dto)));
        when(service.countByUser("U1")).thenReturn(2L);

        assertThat(controller.create("U1", dto).getBody()).isSameAs(dto);
        assertThat(controller.update("U1", "CUSTOMER", 1L, dto).getBody()).isSameAs(dto);
        assertThat(controller.get("U1", "CUSTOMER", 1L).getBody()).isSameAs(dto);
        assertThat(controller.list("U1", "CUSTOMER", "Thunder", "FOOTBALL", 0, 10, "id", "asc")
                .getBody().getContent()).containsExactly(dto);
        assertThat(controller.myCount("U1").getBody()).containsEntry("totalTeams", 2L);
        assertThat(controller.delete("U1", "CUSTOMER", 1L).getBody()).containsEntry("message", "Team deleted");
        verify(service).delete("U1", "CUSTOMER", 1L);
    }
}
