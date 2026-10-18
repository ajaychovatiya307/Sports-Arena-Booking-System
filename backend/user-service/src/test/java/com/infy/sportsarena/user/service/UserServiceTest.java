package com.infy.sportsarena.user.service;

import com.infy.sportsarena.user.dto.CustomerProfileDTO;
import com.infy.sportsarena.user.entity.CustomerProfile;
import com.infy.sportsarena.user.repository.CustomerProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
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
class UserServiceTest {

    @Mock
    private CustomerProfileRepository repository;

    @Mock
    private RestTemplate restTemplate;

    private final ModelMapper mapper = new ModelMapper();

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        setField("repository", repository);
        setField("mapper", mapper);
        setField("restTemplate", restTemplate);
    }

    private void setField(String name, Object value) {
        try {
            var f = UserService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(userService, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void createInternal_returnsExistingProfileWithoutSavingAgain() {
        CustomerProfile existing = new CustomerProfile();
        existing.setUserId("U1");
        existing.setFullName("Existing");
        existing.setEmail("e@x.com");
        when(repository.existsByUserId("U1")).thenReturn(true);
        when(repository.findByUserId("U1")).thenReturn(Optional.of(existing));

        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setUserId("U1");
        CustomerProfileDTO result = userService.createInternal(dto);

        assertThat(result.getFullName()).isEqualTo("Existing");
        verify(repository, never()).save(any());
    }

    @Test
    void createInternal_createsNewProfileWhenAbsent() {
        when(repository.existsByUserId("U2")).thenReturn(false);
        when(repository.save(any(CustomerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setUserId("U2");
        dto.setFullName("New Guy");
        dto.setEmail("new@x.com");
        CustomerProfileDTO result = userService.createInternal(dto);

        assertThat(result.getUserId()).isEqualTo("U2");
        assertThat(result.getFullName()).isEqualTo("New Guy");
    }

    @Test
    void getByUserId_throwsWhenNotFound() {
        when(repository.findByUserId("U9")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getByUserId("U9"));
    }

    @Test
    void getByUserId_returnsMappedProfile() {
        CustomerProfile p = new CustomerProfile();
        p.setUserId("U1");
        p.setFullName("Jane");
        when(repository.findByUserId("U1")).thenReturn(Optional.of(p));

        CustomerProfileDTO dto = userService.getByUserId("U1");

        assertThat(dto.getFullName()).isEqualTo("Jane");
    }

    @Test
    void updateProfile_updatesFieldsAndSyncsName() {
        CustomerProfile p = new CustomerProfile();
        p.setUserId("U1");
        p.setFullName("Old");
        when(repository.findByUserId("U1")).thenReturn(Optional.of(p));
        when(repository.save(any(CustomerProfile.class))).thenAnswer(inv -> inv.getArgument(0));

        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setFullName("New Name");
        dto.setContactNumber("12345");
        dto.setAddress("Addr");

        CustomerProfileDTO result = userService.updateProfile("U1", dto);

        assertThat(result.getFullName()).isEqualTo("New Name");
        verify(restTemplate).put(eq("http://auth-service/api/auth/internal/profile"), any());
    }

    @Test
    void updateProfile_doesNotFailWhenAuthSyncThrows() {
        CustomerProfile p = new CustomerProfile();
        p.setUserId("U1");
        when(repository.findByUserId("U1")).thenReturn(Optional.of(p));
        when(repository.save(any(CustomerProfile.class))).thenAnswer(inv -> inv.getArgument(0));
        doThrow(new RuntimeException("down")).when(restTemplate).put(anyString(), any());

        CustomerProfileDTO dto = new CustomerProfileDTO();
        dto.setFullName("New Name");

        CustomerProfileDTO result = userService.updateProfile("U1", dto);

        assertThat(result.getFullName()).isEqualTo("New Name");
    }

    @Test
    void listAll_delegatesToSearchQueryWhenProvided() {
        Page<CustomerProfile> page = new PageImpl<>(List.of(new CustomerProfile()));
        when(repository
                .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserIdContainingIgnoreCase(
                        eq("jane"), eq("jane"), eq("jane"), any(Pageable.class)))
                .thenReturn(page);

        Page<CustomerProfileDTO> result = userService.listAll("jane", 0, 10, "fullName", "asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void listAll_usesFindAllWhenNoQuery() {
        Page<CustomerProfile> page = new PageImpl<>(List.of(new CustomerProfile()));
        when(repository.findAll(any(Pageable.class))).thenReturn(page);

        Page<CustomerProfileDTO> result = userService.listAll(null, 0, 10, "fullName", "desc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void count_delegatesToRepository() {
        when(repository.count()).thenReturn(5L);
        assertThat(userService.count()).isEqualTo(5L);
    }
}
