package com.infy.sportsarena.catalog.service;

import com.infy.sportsarena.catalog.dto.SlotAvailabilityDTO;
import com.infy.sportsarena.catalog.entity.SlotAvailability;
import com.infy.sportsarena.catalog.repository.SlotAvailabilityRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SlotAvailabilityServiceTest {

    @Mock
    private SlotAvailabilityRepository repo;
    @Mock
    private RestTemplate restTemplate;

    private SlotAvailabilityService service;

    @BeforeEach
    void setUp() {
        service = new SlotAvailabilityService();
        setField("repo", repo);
        setField("mapper", new ModelMapper());
        setField("restTemplate", restTemplate);
    }

    private void setField(String name, Object value) {
        try {
            var f = SlotAvailabilityService.class.getDeclaredField(name);
            f.setAccessible(true);
            f.set(service, value);
        } catch (ReflectiveOperationException e) {
            throw new RuntimeException(e);
        }
    }

    private SlotAvailabilityDTO sampleDto(boolean blocked) {
        SlotAvailabilityDTO dto = new SlotAvailabilityDTO();
        dto.setPackageId(1L);
        dto.setSlotDate(LocalDate.of(2026, 10, 10));
        dto.setTimeSlot("10:00-11:00");
        dto.setBlocked(blocked);
        dto.setReason("Maintenance");
        return dto;
    }

    @Test
    void upsert_throwsWhenBlockingSlotWithActiveBooking() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(true);

        assertThrows(RuntimeException.class, () -> service.upsert(sampleDto(true)));
        verify(repo, never()).save(any());
    }

    @Test
    void upsert_createsNewSlotWhenNoneExists() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class))).thenReturn(false);
        when(repo.findByPackageIdAndSlotDateAndTimeSlot(1L, LocalDate.of(2026, 10, 10), "10:00-11:00"))
                .thenReturn(Optional.empty());
        when(repo.save(any(SlotAvailability.class))).thenAnswer(inv -> inv.getArgument(0));

        SlotAvailabilityDTO result = service.upsert(sampleDto(true));

        assertThat(result.isBlocked()).isTrue();
        assertThat(result.getReason()).isEqualTo("Maintenance");
    }

    @Test
    void upsert_doesNotCheckActiveBookingWhenUnblocking() {
        when(repo.findByPackageIdAndSlotDateAndTimeSlot(1L, LocalDate.of(2026, 10, 10), "10:00-11:00"))
                .thenReturn(Optional.empty());
        when(repo.save(any(SlotAvailability.class))).thenAnswer(inv -> inv.getArgument(0));

        SlotAvailabilityDTO result = service.upsert(sampleDto(false));

        assertThat(result.isBlocked()).isFalse();
        verifyNoInteractions(restTemplate);
    }

    @Test
    void upsert_treatsDownstreamFailureAsNoActiveBooking() {
        when(restTemplate.getForObject(anyString(), eq(Boolean.class)))
                .thenThrow(new RuntimeException("down"));
        when(repo.findByPackageIdAndSlotDateAndTimeSlot(1L, LocalDate.of(2026, 10, 10), "10:00-11:00"))
                .thenReturn(Optional.empty());
        when(repo.save(any(SlotAvailability.class))).thenAnswer(inv -> inv.getArgument(0));

        SlotAvailabilityDTO result = service.upsert(sampleDto(true));

        assertThat(result.isBlocked()).isTrue();
    }

    @Test
    void delete_throwsWhenSlotNotFound() {
        when(repo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> service.delete(1L));
    }

    @Test
    void delete_removesExistingSlot() {
        SlotAvailability s = new SlotAvailability();
        when(repo.findById(1L)).thenReturn(Optional.of(s));

        service.delete(1L);

        verify(repo).delete(s);
    }

    @Test
    void listForPackageAndDate_returnsMappedSlots() {
        SlotAvailability s = new SlotAvailability();
        s.setTimeSlot("09:00-10:00");
        when(repo.findByPackageIdAndSlotDate(1L, LocalDate.of(2026, 10, 10))).thenReturn(List.of(s));

        List<SlotAvailabilityDTO> result = service.listForPackageAndDate(1L, LocalDate.of(2026, 10, 10));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTimeSlot()).isEqualTo("09:00-10:00");
    }

    @Test
    void isBlocked_returnsFalseWhenSlotMissing() {
        when(repo.findByPackageIdAndSlotDateAndTimeSlot(1L, LocalDate.of(2026, 10, 10), "10:00-11:00"))
                .thenReturn(Optional.empty());

        assertThat(service.isBlocked(1L, LocalDate.of(2026, 10, 10), "10:00-11:00")).isFalse();
    }

    @Test
    void isBlocked_returnsStoredValue() {
        SlotAvailability s = new SlotAvailability();
        s.setBlocked(true);
        when(repo.findByPackageIdAndSlotDateAndTimeSlot(1L, LocalDate.of(2026, 10, 10), "10:00-11:00"))
                .thenReturn(Optional.of(s));

        assertThat(service.isBlocked(1L, LocalDate.of(2026, 10, 10), "10:00-11:00")).isTrue();
    }
}
