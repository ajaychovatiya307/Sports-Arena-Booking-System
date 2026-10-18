package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.ArenaPackageDTO;
import com.infy.sportsarena.catalog.entity.ArenaCategory;
import com.infy.sportsarena.catalog.entity.ArenaPackage;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import com.infy.sportsarena.catalog.repository.ArenaCategoryRepository;
import com.infy.sportsarena.catalog.repository.ArenaPackageRepository;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArenaPackageServiceTest {

    @Mock
    private ArenaPackageRepository packageRepo;
    @Mock
    private ArenaCategoryRepository categoryRepo;
    @Mock
    private RestTemplate restTemplate;

    private ArenaPackageService service;

    @BeforeEach
    void setUp() {
        service = new ArenaPackageService();
        setField("packageRepo", packageRepo);
        setField("categoryRepo", categoryRepo);
        setField("mapper", new ModelMapper());
        setField("restTemplate", restTemplate);
    }

    private void setField(String name, Object value) {
        try {
            var f = ArenaPackageService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private ArenaPackageDTO sampleDto() {
        ArenaPackageDTO dto = new ArenaPackageDTO();
        dto.setName("Evening Slot");
        dto.setPricePerHour(new BigDecimal("500"));
        dto.setDurationHours(1);
        dto.setCategoryId(1L);
        return dto;
    }

    @Test
    void create_throwsWhenCategoryNotFound() {
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.create(sampleDto()));
    }

    @Test
    void create_savesWithGeneratedCode() {
        ArenaCategory cat = new ArenaCategory();
        cat.setId(1L);
        cat.setName("Football");
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(cat));
        when(packageRepo.count()).thenReturn(4L);
        when(packageRepo.save(any(ArenaPackage.class))).thenAnswer(inv -> inv.getArgument(0));

        ArenaPackageDTO result = service.create(sampleDto());

        assertThat(result.getPackageCode()).isEqualTo("PKG005");
        assertThat(result.getStatus()).isEqualTo(AvailabilityStatus.ACTIVE);
        assertThat(result.getCategoryName()).isEqualTo("Football");
    }

    @Test
    void update_throwsWhenPackageNotFound() {
        when(packageRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(1L, sampleDto()));
    }

    @Test
    void update_throwsWhenCategoryNotFound() {
        ArenaPackage existing = new ArenaPackage();
        when(packageRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(1L, sampleDto()));
    }

    @Test
    void updateMapsUpdatedPackageAndPreservesStatusWhenOmitted() {
        ArenaCategory category = new ArenaCategory();
        category.setId(1L);
        category.setName("Football");
        ArenaPackage existing = new ArenaPackage();
        existing.setStatus(AvailabilityStatus.INACTIVE);
        when(packageRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
        when(packageRepo.save(any(ArenaPackage.class))).thenAnswer(inv -> inv.getArgument(0));

        ArenaPackageDTO result = service.update(1L, sampleDto());

        assertThat(result.getStatus()).isEqualTo(AvailabilityStatus.INACTIVE);
        assertThat(result.getName()).isEqualTo("Evening Slot");
        assertThat(result.getCategoryName()).isEqualTo("Football");
    }

    @Test
    void delete_throwsWhenActiveBookingsAndDeactivatesInstead() {
        ArenaPackage p = new ArenaPackage();
        p.setId(1L);
        when(packageRepo.findById(1L)).thenReturn(Optional.of(p));
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.delete(1L));

        assertThat(p.getStatus()).isEqualTo(AvailabilityStatus.INACTIVE);
        verify(packageRepo).save(p);
        verify(packageRepo, never()).delete(any());
    }

    @Test
    void delete_hardDeletesWhenNoActiveBookings() {
        ArenaPackage p = new ArenaPackage();
        p.setId(1L);
        when(packageRepo.findById(1L)).thenReturn(Optional.of(p));
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(false);

        service.delete(1L);

        verify(packageRepo).delete(p);
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(packageRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }

    @Test
    void getByIdReturnsMappedPackage() {
        ArenaCategory category = new ArenaCategory();
        category.setId(1L);
        category.setName("Football");
        ArenaPackage arenaPackage = new ArenaPackage();
        arenaPackage.setCategory(category);
        arenaPackage.setName("Court");
        when(packageRepo.findById(1L)).thenReturn(Optional.of(arenaPackage));

        ArenaPackageDTO result = service.getById(1L);

        assertThat(result.getName()).isEqualTo("Court");
        assertThat(result.getCategoryId()).isEqualTo(1L);
        assertThat(result.getCategoryName()).isEqualTo("Football");
    }

    @Test
    void list_activeOnlyWithCategoryFilter() {
        Page<ArenaPackage> page = new PageImpl<>(List.of(new ArenaPackage()));
        when(packageRepo.findByStatusAndCategoryId(eq(AvailabilityStatus.ACTIVE), eq(1L), any(Pageable.class)))
                .thenReturn(page);

        Page<ArenaPackageDTO> result = service.list(null, 1L, true, 0, 10, "name", "asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void list_allWithSearchQuery() {
        Page<ArenaPackage> page = new PageImpl<>(List.of(new ArenaPackage()));
        when(packageRepo.findByNameContainingIgnoreCase(eq("evening"), any(Pageable.class))).thenReturn(page);

        Page<ArenaPackageDTO> result = service.list("evening", null, false, 0, 10, "name", "asc");

        assertThat(result.getTotalElements()).isEqualTo(1);
    }

    @Test
    void list_activeOnlyWithSearchQuery() {
        when(packageRepo.findByStatusAndNameContainingIgnoreCase(eq(AvailabilityStatus.ACTIVE),
                eq("court"), any(Pageable.class))).thenReturn(Page.empty());
        assertThat(service.list("court", null, true, 0, 10, "name", "asc")).isEmpty();
    }

    @Test
    void list_activeOnlyWithoutFilters() {
        when(packageRepo.findByStatus(eq(AvailabilityStatus.ACTIVE), any(Pageable.class)))
                .thenReturn(Page.empty());
        assertThat(service.list(null, null, true, 0, 10, "name", "asc")).isEmpty();
    }

    @Test
    void list_allWithoutSearchQuery() {
        when(packageRepo.findAll(any(Pageable.class))).thenReturn(Page.empty());
        assertThat(service.list(null, null, false, 0, 10, "pricePerHour", "desc")).isEmpty();
    }

    @Test
    void bookingCheckFallbackFailsSafe() throws Exception {
        var fallback = ArenaPackageService.class.getDeclaredMethod(
                "hasActiveBookingsFallback", Long.class, Throwable.class);
        fallback.setAccessible(true);
        assertThat(fallback.invoke(service, 1L, new RuntimeException("booking service down"))).isEqualTo(true);
    }

    @Test
    void countActive_delegatesToRepository() {
        when(packageRepo.countByStatus(AvailabilityStatus.ACTIVE)).thenReturn(3L);
        assertThat(service.countActive()).isEqualTo(3L);
    }
}
