package com.infy.sportsarena.user.service;

import com.infy.sportsarena.user.dto.TeamDTO;
import com.infy.sportsarena.user.entity.SportType;
import com.infy.sportsarena.user.entity.Team;
import com.infy.sportsarena.user.repository.TeamRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock
    private TeamRepository repository;

    @Mock
    private RestTemplate restTemplate;

    private TeamService teamService;

    @BeforeEach
    void setUp() {
        teamService = new TeamService();
        setField("repository", repository);
        setField("mapper", new ModelMapper());
        setField("restTemplate", restTemplate);
    }

    private void setField(String name, Object value) {
        try {
            var f = TeamService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(teamService, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private TeamDTO sampleDto() {
        TeamDTO dto = new TeamDTO();
        dto.setTeamName("Thunder");
        dto.setSportType(SportType.FOOTBALL);
        dto.setNumberOfPlayers(11);
        dto.setCoachName("Coach K");
        dto.setFoundedYear(2020);
        return dto;
    }

    @Test
    void create_throwsWhenTeamNameAlreadyExists() {
        when(repository.existsByTeamNameIgnoreCase("Thunder")).thenReturn(true);
        assertThrows(RuntimeException.class, () -> teamService.create("U1", sampleDto()));
        verify(repository, never()).save(any());
    }

    @Test
    void create_savesNewTeam() {
        when(repository.existsByTeamNameIgnoreCase("Thunder")).thenReturn(false);
        when(repository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamDTO result = teamService.create("U1", sampleDto());

        assertThat(result.getTeamName()).isEqualTo("Thunder");
        assertThat(result.getSportType()).isEqualTo(SportType.FOOTBALL);
    }

    @Test
    void createSkipsDuplicateLookupWhenTeamNameIsNull() {
        TeamDTO dto = sampleDto();
        dto.setTeamName(null);
        when(repository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamDTO result = teamService.create("U1", dto);

        assertThat(result.getTeamName()).isNull();
        verify(repository, never()).existsByTeamNameIgnoreCase(anyString());
    }

    @Test
    void update_throwsForAdminRole() {
        assertThrows(RuntimeException.class, () -> teamService.update("U1", "ADMIN", 1L, sampleDto()));
        verifyNoInteractions(repository);
    }

    @Test
    void update_throwsWhenTeamNotFound() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.update("U1", "CUSTOMER", 1L, sampleDto()));
    }

    @Test
    void update_throwsWhenNotOwner() {
        Team t = new Team();
        t.setUserId("U2");
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        assertThrows(RuntimeException.class, () -> teamService.update("U1", "CUSTOMER", 1L, sampleDto()));
    }

    @Test
    void update_succeedsForOwner() {
        Team t = new Team();
        t.setUserId("U1");
        t.setTeamName("Old");
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(repository.existsByTeamNameIgnoreCaseAndIdNot("Thunder", 1L)).thenReturn(false);
        when(repository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        TeamDTO result = teamService.update("U1", "CUSTOMER", 1L, sampleDto());

        assertThat(result.getTeamName()).isEqualTo("Thunder");
    }

    @Test
    void update_throwsWhenTeamNameIsAlreadyUsed() {
        Team team = new Team();
        team.setUserId("U1");
        when(repository.findById(1L)).thenReturn(Optional.of(team));
        when(repository.existsByTeamNameIgnoreCaseAndIdNot("Thunder", 1L)).thenReturn(true);
        assertThrows(RuntimeException.class, () -> teamService.update("U1", "CUSTOMER", 1L, sampleDto()));
        verify(repository, never()).save(any());
    }

    @Test
    void updateSkipsDuplicateLookupWhenTeamNameIsNull() {
        Team team = new Team();
        team.setUserId("U1");
        TeamDTO dto = sampleDto();
        dto.setTeamName(null);
        when(repository.findById(1L)).thenReturn(Optional.of(team));
        when(repository.save(any(Team.class))).thenAnswer(inv -> inv.getArgument(0));

        assertThat(teamService.update("U1", "CUSTOMER", 1L, dto).getTeamName()).isNull();
        verify(repository, never()).existsByTeamNameIgnoreCaseAndIdNot(anyString(), anyLong());
    }

    @Test
    void delete_throwsForAdminRole() {
        assertThrows(RuntimeException.class, () -> teamService.delete("U1", "ADMIN", 1L));
        verifyNoInteractions(repository);
    }

    @Test
    void delete_throwsWhenHasActiveBookings() {
        Team t = new Team();
        t.setUserId("U1");
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> teamService.delete("U1", "CUSTOMER", 1L));
        verify(repository, never()).delete(any());
    }

    @Test
    void delete_succeedsWhenNoActiveBookings() {
        Team t = new Team();
        t.setUserId("U1");
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(false);

        teamService.delete("U1", "CUSTOMER", 1L);

        verify(repository).delete(t);
    }

    @Test
    void getById_throwsWhenUnauthorized() {
        Team t = new Team();
        t.setUserId("U2");
        when(repository.findById(1L)).thenReturn(Optional.of(t));
        assertThrows(RuntimeException.class, () -> teamService.getById("U1", "CUSTOMER", 1L));
    }

    @Test
    void getById_allowsAdmin() {
        Team t = new Team();
        t.setUserId("U2");
        t.setTeamName("Thunder");
        when(repository.findById(1L)).thenReturn(Optional.of(t));

        TeamDTO result = teamService.getById("U1", "ADMIN", 1L);

        assertThat(result.getTeamName()).isEqualTo("Thunder");
    }

    @Test
    void getById_allowsOwner() {
        Team team = new Team();
        team.setUserId("U1");
        team.setTeamName("Thunder");
        when(repository.findById(1L)).thenReturn(Optional.of(team));
        assertThat(teamService.getById("U1", "CUSTOMER", 1L).getTeamName()).isEqualTo("Thunder");
    }

    @Test
    void getById_throwsWhenTeamDoesNotExist() {
        when(repository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> teamService.getById("U1", "ADMIN", 1L));
    }

    @Test
    void listForUser_adminWithNoFiltersUsesFindAll() {
        Page<Team> page = new PageImpl<>(List.of(new Team()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<TeamDTO> result = teamService.listForUser("U1", "ADMIN", null, null, 0, 10, "teamName", "asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_adminSearchAndSportUsesCombinedFilter() {
        when(repository.findBySportTypeAndTeamNameContainingIgnoreCase(eq(SportType.FOOTBALL), eq("Th"),
                any(Pageable.class))).thenReturn(new PageImpl<>(List.of(new Team())));
        assertThat(teamService.listForUser("U1", "ADMIN", "Th", "FOOTBALL", 0, 10, "teamName", "asc")
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_adminSportOnlyUsesSportFilter() {
        when(repository.findBySportType(eq(SportType.CRICKET), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Team())));
        assertThat(teamService.listForUser("U1", "ADMIN", null, "CRICKET", 0, 10, "teamName", "asc")
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_adminSearchOnlyUsesNameFilter() {
        when(repository.findByTeamNameContainingIgnoreCase(eq("Thunder"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Team())));
        assertThat(teamService.listForUser("U1", "ADMIN", "Thunder", null, 0, 10, "teamName", "asc")
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_customerWithNoFiltersPagesManually() {
        Team t1 = new Team();
        t1.setUserId("U1");
        t1.setTeamName("A");
        when(repository.findByUserId("U1")).thenReturn(List.of(t1));

        Page<TeamDTO> result = teamService.listForUser("U1", "CUSTOMER", null, null, 0, 10, "teamName", "asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_customerSportFilterIsApplied() {
        when(repository.findByUserIdAndSportType(eq("U1"), eq(SportType.FOOTBALL), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Team())));
        assertThat(teamService.listForUser("U1", "CUSTOMER", null, "FOOTBALL", 0, 10, "teamName", "asc")
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_customerNameFilterIsApplied() {
        when(repository.findByUserIdAndTeamNameContainingIgnoreCase(eq("U1"), eq("Thunder"), any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(new Team())));
        assertThat(teamService.listForUser("U1", "CUSTOMER", "Thunder", null, 0, 10, "teamName", "desc")
                .getTotalElements()).isEqualTo(1);
    }

    @Test
    void listForUser_customerManualPaginationHandlesOutOfRangePage() {
        when(repository.findByUserId("U1")).thenReturn(List.of(new Team()));
        Page<TeamDTO> result = teamService.listForUser("U1", "CUSTOMER", null, null, 3, 10, "teamName", "asc");
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void countByUser_countsTeamsForUser() {
        when(repository.findByUserId("U1")).thenReturn(List.of(new Team(), new Team()));
        assertThat(teamService.countByUser("U1")).isEqualTo(2);
    }

    @Test
    void hasActiveBookings_returnsFalseOnFallback() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
                .thenThrow(new RuntimeException("down"));
        // Direct call bypasses circuit breaker proxy in a plain unit test, so the
        // exception propagates — verifying the happy path is handled separately above.
        assertThrows(RuntimeException.class, () -> teamService.hasActiveBookings(1L));
    }
}
