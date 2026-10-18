package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.ArenaPackageDTO;
import com.infy.sportsarena.catalog.entity.ArenaCategory;
import com.infy.sportsarena.catalog.entity.ArenaPackage;
import com.infy.sportsarena.catalog.entity.AvailabilityStatus;
import com.infy.sportsarena.catalog.repository.ArenaCategoryRepository;
import com.infy.sportsarena.catalog.repository.ArenaPackageRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class ArenaPackageService {

    @Autowired
    private ArenaPackageRepository packageRepo;
    @Autowired
    private ArenaCategoryRepository categoryRepo;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private RestTemplate restTemplate;

    private ArenaPackageDTO toDto(ArenaPackage p) {
        ArenaPackageDTO d = mapper.map(p, ArenaPackageDTO.class);
        if (p.getCategory() != null) {
            d.setCategoryId(p.getCategory().getId());
            d.setCategoryName(p.getCategory().getName());
        }
        return d;
    }

    public ArenaPackageDTO create(ArenaPackageDTO dto) {
        ArenaCategory cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        ArenaPackage p = new ArenaPackage();
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPricePerHour(dto.getPricePerHour());
        p.setDurationHours(dto.getDurationHours());
        p.setCategory(cat);
        p.setStatus(dto.getStatus() != null ? dto.getStatus() : AvailabilityStatus.ACTIVE);
        p.setPackageCode(String.format("PKG%03d", packageRepo.count() + 1));
        return toDto(packageRepo.save(p));
    }

    public ArenaPackageDTO update(Long id, ArenaPackageDTO dto) {
        ArenaPackage p = packageRepo.findById(id).orElseThrow(() -> new RuntimeException("Arena package not found"));
        ArenaCategory cat = categoryRepo.findById(dto.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Category not found"));
        p.setName(dto.getName());
        p.setDescription(dto.getDescription());
        p.setPricePerHour(dto.getPricePerHour());
        p.setDurationHours(dto.getDurationHours());
        p.setCategory(cat);
        if (dto.getStatus() != null)
            p.setStatus(dto.getStatus());
        return toDto(packageRepo.save(p));
    }

    public void delete(Long id) {
        ArenaPackage p = packageRepo.findById(id).orElseThrow(() -> new RuntimeException("Arena package not found"));
        if (hasActiveBookings(id)) {
            // Deactivate rather than hard-delete
            p.setStatus(AvailabilityStatus.INACTIVE);
            packageRepo.save(p);
            throw new RuntimeException("Package has active bookings and was deactivated instead of deleted");
        }
        packageRepo.delete(p);
    }

    public ArenaPackageDTO getById(Long id) {
        return toDto(packageRepo.findById(id).orElseThrow(() -> new RuntimeException("Arena package not found")));
    }

    public Page<ArenaPackageDTO> list(String q, Long categoryId, Boolean activeOnly,
            int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<ArenaPackage> result;

        boolean active = Boolean.TRUE.equals(activeOnly);
        if (active) {
            if (categoryId != null)
                result = packageRepo.findByStatusAndCategoryId(AvailabilityStatus.ACTIVE, categoryId, pageable);
            else if (q != null && !q.isBlank())
                result = packageRepo.findByStatusAndNameContainingIgnoreCase(AvailabilityStatus.ACTIVE, q, pageable);
            else
                result = packageRepo.findByStatus(AvailabilityStatus.ACTIVE, pageable);
        } else {
            if (q != null && !q.isBlank())
                result = packageRepo.findByNameContainingIgnoreCase(q, pageable);
            else
                result = packageRepo.findAll(pageable);
        }
        return result.map(this::toDto);
    }

    public long countActive() {
        return packageRepo.countByStatus(AvailabilityStatus.ACTIVE);
    }

    @CircuitBreaker(name = "bookingService", fallbackMethod = "hasActiveBookingsFallback")
    public boolean hasActiveBookings(Long packageId) {
        Boolean b = restTemplate.getForObject(
                "http://booking-service/api/bookings/internal/package/" + packageId + "/active-exists",
                Boolean.class);
        return Boolean.TRUE.equals(b);
    }

    @SuppressWarnings("unused")
    private boolean hasActiveBookingsFallback(Long packageId, Throwable ex) {
        return true;
    }
}
