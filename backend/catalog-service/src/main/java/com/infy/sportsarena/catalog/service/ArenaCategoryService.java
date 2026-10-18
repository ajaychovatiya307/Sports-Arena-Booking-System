package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.ArenaCategoryDTO;
import com.infy.sportsarena.catalog.entity.ArenaCategory;
import com.infy.sportsarena.catalog.entity.ArenaPackage;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import com.infy.sportsarena.catalog.repository.ArenaCategoryRepository;
import com.infy.sportsarena.catalog.repository.ArenaPackageRepository;
import com.infy.sportsarena.catalog.repository.SlotAvailabilityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ArenaCategoryService {

    @Autowired private ArenaCategoryRepository categoryRepo;
    @Autowired private ArenaPackageRepository packageRepo;
    @Autowired private SlotAvailabilityRepository slotRepo;
    @Autowired private ModelMapper mapper;

    public ArenaCategoryDTO create(ArenaCategoryDTO dto) {
        if (categoryRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
        }
        ArenaCategory c = new ArenaCategory();
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        c.setCategoryCode(generateCode());
        return mapper.map(categoryRepo.save(c), ArenaCategoryDTO.class);
    }

    private String generateCode() {
        long next = categoryRepo.count() + 1;
        return String.format("CAT%03d", next);
    }

    public ArenaCategoryDTO update(Long id, ArenaCategoryDTO dto) {
        ArenaCategory c = categoryRepo.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        if (!c.getName().equals(dto.getName()) && categoryRepo.existsByName(dto.getName())) {
            throw new RuntimeException("Category with name '" + dto.getName() + "' already exists");
        }
        c.setName(dto.getName());
        c.setDescription(dto.getDescription());
        return mapper.map(categoryRepo.save(c), ArenaCategoryDTO.class);
    }

    @Transactional
    public void delete(Long id) {
        ArenaCategory c = categoryRepo.findById(id).orElseThrow(() -> new RuntimeException("Category not found"));
        long activePackages = packageRepo.countByCategoryIdAndStatus(id, AvailabilityStatus.ACTIVE);
        if (activePackages > 0) {
            throw new RuntimeException("Cannot delete category with active arena packages");
        }
        // SRS: only categories WITHOUT active packages may be deleted. Any remaining
        // packages are INACTIVE — remove them (and their slot availability) first so the
        // foreign-key constraint on arena_package.category_id does not block the delete.
        List<ArenaPackage> remaining = packageRepo.findByCategoryId(id);
        for (ArenaPackage p : remaining) {
            slotRepo.deleteByPackageId(p.getId());
        }
        packageRepo.deleteAll(remaining);
        categoryRepo.delete(c);
    }

    public List<ArenaCategoryDTO> list() {
        return categoryRepo.findAll().stream()
                .map(c -> mapper.map(c, ArenaCategoryDTO.class))
                .toList();
    }

    public ArenaCategoryDTO getById(Long id) {
        return mapper.map(
                categoryRepo.findById(id).orElseThrow(() -> new RuntimeException("Category not found")),
                ArenaCategoryDTO.class);
    }

    public long count() { return categoryRepo.count(); }
}
