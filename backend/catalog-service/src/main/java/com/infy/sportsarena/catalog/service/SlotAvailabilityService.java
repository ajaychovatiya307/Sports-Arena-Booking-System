package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.SlotAvailabilityDTO;
import com.infy.sportsarena.catalog.entity.SlotAvailability;
import com.infy.sportsarena.catalog.repository.SlotAvailabilityRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public class SlotAvailabilityService {

    @Autowired private SlotAvailabilityRepository repo;
    @Autowired private ModelMapper mapper;
    @Autowired private RestTemplate restTemplate;

    public SlotAvailabilityDTO upsert(SlotAvailabilityDTO dto) {
        // SRS US11: A time slot already held by a non-cancelled customer booking
        // cannot be blocked. The administrator must cancel the booking first
        // (only PENDING bookings can be cancelled) before the slot can be blocked.
        if (dto.isBlocked() && slotHasActiveBooking(dto.getPackageId(), dto.getSlotDate(), dto.getTimeSlot())) {
            throw new RuntimeException("Please cancel the booking and then block the slot");
        }
        SlotAvailability s = repo.findByPackageIdAndSlotDateAndTimeSlot(
                dto.getPackageId(), dto.getSlotDate(), dto.getTimeSlot())
                .orElseGet(SlotAvailability::new);
        s.setPackageId(dto.getPackageId());
        s.setSlotDate(dto.getSlotDate());
        s.setTimeSlot(dto.getTimeSlot());
        s.setBlocked(dto.isBlocked());
        s.setReason(dto.getReason());
        return mapper.map(repo.save(s), SlotAvailabilityDTO.class);
    }

    @SuppressWarnings("unchecked")
    private boolean slotHasActiveBooking(Long packageId, LocalDate date, String timeSlot) {
        try {
            String url = "http://booking-service/api/bookings/internal/slot/active-exists?packageId="
                    + packageId + "&date=" + date + "&timeSlot=" + timeSlot;
            Boolean result = restTemplate.getForObject(url, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (RuntimeException ex) {
            // If booking-service is unreachable, do not force an override on a false positive.
            return false;
        }
    }

    public void delete(Long id) {
        SlotAvailability s = repo.findById(id).orElseThrow(() -> new RuntimeException("Slot not found"));
        repo.delete(s);
    }

    public List<SlotAvailabilityDTO> listForPackageAndDate(Long packageId, LocalDate date) {
        return repo.findByPackageIdAndSlotDate(packageId, date).stream()
                .map(s -> mapper.map(s, SlotAvailabilityDTO.class))
                .toList();
    }

    public boolean isBlocked(Long packageId, LocalDate date, String timeSlot) {
        return repo.findByPackageIdAndSlotDateAndTimeSlot(packageId, date, timeSlot)
                .map(SlotAvailability::isBlocked)
                .orElse(false);
    }
}
