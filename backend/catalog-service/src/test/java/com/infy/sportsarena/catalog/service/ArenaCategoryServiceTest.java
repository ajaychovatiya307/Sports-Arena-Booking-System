package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.ArenaCategoryDTO;
import com.infy.sportsarena.catalog.entity.ArenaCategory;
import com.infy.sportsarena.catalog.entity.ArenaPackage;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import com.infy.sportsarena.catalog.repository.ArenaCategoryRepository;
import com.infy.sportsarena.catalog.repository.ArenaPackageRepository;
import com.infy.sportsarena.catalog.repository.SlotAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ArenaCategoryServiceTest {

    @Mock
    private ArenaCategoryRepository categoryRepo;
    @Mock
    private ArenaPackageRepository packageRepo;
    @Mock
    private SlotAvailabilityRepository slotRepo;

    private ArenaCategoryService service;

    @BeforeEach
    void setUp() {
        service = new ArenaCategoryService();
        setField("categoryRepo", categoryRepo);
        setField("packageRepo", packageRepo);
        setField("slotRepo", slotRepo);
        setField("mapper", new ModelMapper());
    }

    private void setField(String name, Object value) {
        try {
            var f = ArenaCategoryService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void create_throwsWhenNameExists() {
        ArenaCategoryDTO dto = new ArenaCategoryDTO();
        dto.setName("Football");
        when(categoryRepo.existsByName("Football")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.create(dto));
        verify(categoryRepo, never()).save(any());
    }

    @Test
    void create_generatesCodeAndSaves() {
        ArenaCategoryDTO dto = new ArenaCategoryDTO();
        dto.setName("Football");
        dto.setDescription("Football arenas");
        when(categoryRepo.existsByName("Football")).thenReturn(false);
        when(categoryRepo.count()).thenReturn(2L);
        when(categoryRepo.save(any(ArenaCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ArenaCategoryDTO result = service.create(dto);

        assertThat(result.getCategoryCode()).isEqualTo("CAT003");
        assertThat(result.getName()).isEqualTo("Football");
    }

    @Test
    void update_throwsWhenCategoryNotFound() {
        when(categoryRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.update(1L, new ArenaCategoryDTO()));
    }

    @Test
    void update_throwsWhenRenamedToExistingName() {
        ArenaCategory existing = new ArenaCategory();
        existing.setName("Football");
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.existsByName("Cricket")).thenReturn(true);

        ArenaCategoryDTO dto = new ArenaCategoryDTO();
        dto.setName("Cricket");

        assertThrows(RuntimeException.class, () -> service.update(1L, dto));
    }

    @Test
    void update_savesWhenValid() {
        ArenaCategory existing = new ArenaCategory();
        existing.setName("Football");
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(existing));
        when(categoryRepo.save(any(ArenaCategory.class))).thenAnswer(inv -> inv.getArgument(0));

        ArenaCategoryDTO dto = new ArenaCategoryDTO();
        dto.setName("Football");
        dto.setDescription("Updated");

        ArenaCategoryDTO result = service.update(1L, dto);

        assertThat(result.getDescription()).isEqualTo("Updated");
    }

    @Test
    void delete_throwsWhenActivePackagesExist() {
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(new ArenaCategory()));
        when(packageRepo.countByCategoryIdAndStatus(1L, AvailabilityStatus.ACTIVE)).thenReturn(1L);

        assertThrows(RuntimeException.class, () -> service.delete(1L));
        verify(categoryRepo, never()).delete(any());
    }

    @Test
    void delete_removesInactivePackagesThenDeletesCategory() {
        ArenaCategory c = new ArenaCategory();
        ArenaPackage p = new ArenaPackage();
        p.setId(5L);
        when(categoryRepo.findById(1L)).thenReturn(Optional.of(c));
        when(packageRepo.countByCategoryIdAndStatus(1L, AvailabilityStatus.ACTIVE)).thenReturn(0L);
        when(packageRepo.findByCategoryId(1L)).thenReturn(List.of(p));

        service.delete(1L);

        verify(slotRepo).deleteByPackageId(5L);
        verify(packageRepo).deleteAll(List.of(p));
        verify(categoryRepo).delete(c);
    }

    @Test
    void list_returnsMappedCategories() {
        ArenaCategory c = new ArenaCategory();
        c.setName("Football");
        when(categoryRepo.findAll()).thenReturn(List.of(c));

        List<ArenaCategoryDTO> result = service.list();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Football");
    }

    @Test
    void getById_throwsWhenNotFound() {
        when(categoryRepo.findById(anyLong())).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.getById(1L));
    }

    @Test
    void count_delegatesToRepository() {
        when(categoryRepo.count()).thenReturn(7L);
        assertThat(service.count()).isEqualTo(7L);
    }
}
