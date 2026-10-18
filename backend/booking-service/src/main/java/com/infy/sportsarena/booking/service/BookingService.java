package com.infy.sportsarena.booking.service;

import com.infy.sportsarena.booking.dto.BookingDTO;
import com.infy.sportsarena.booking.dto.DashboardSummaryDTO;
import com.infy.sportsarena.booking.entity.Booking;
import com.infy.sportsarena.booking.entity.BookingStatus;
import com.infy.sportsarena.booking.repository.BookingRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Service
public class BookingService {

    private static final List<BookingStatus> ACTIVE_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN);

    // Any booking that still holds its slot (everything except CANCELLED).
    private static final List<BookingStatus> NON_CANCELLED_STATUSES =
            List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED, BookingStatus.CHECKED_IN, BookingStatus.COMPLETED);

    // Allowed transitions per SRS: PENDING -> CONFIRMED | CANCELLED; CONFIRMED -> CHECKED_IN | CANCELLED;
    //                             CHECKED_IN -> COMPLETED
    private static final Map<BookingStatus, Set<BookingStatus>> TRANSITIONS = Map.of(
            BookingStatus.PENDING, Set.of(BookingStatus.CONFIRMED, BookingStatus.CANCELLED),
            BookingStatus.CONFIRMED, Set.of(BookingStatus.CHECKED_IN, BookingStatus.CANCELLED),
            BookingStatus.CHECKED_IN, Set.of(BookingStatus.COMPLETED),
            BookingStatus.COMPLETED, Set.of(),
            BookingStatus.CANCELLED, Set.of()
    );

    @Autowired private BookingRepository repository;
    @Autowired private ModelMapper mapper;
    @Autowired private RestTemplate restTemplate;

    public BookingDTO create(String userId, String role, BookingDTO dto) {
        // SRS US07: Arena Slot Booking is a Customer action. Administrators have
        // management/read-only responsibilities and must not create bookings on
        // behalf of customers (there is deliberately no admin-create path).
        if ("ADMIN".equals(role)) {
            throw new RuntimeException("Administrators cannot create bookings. Booking is a customer action.");
        }
        if (dto.getBookingDate() == null || dto.getBookingDate().isBefore(LocalDate.now())) {
            throw new RuntimeException("Booking date must be today or in the future");
        }

        // Fetch team + package details (best effort via circuit breaker fallback)
        Map<String, Object> team = fetchTeam(dto.getTeamId(), userId, role);
        if (team.isEmpty()) throw new RuntimeException("Team not found");
        String teamOwner = String.valueOf(team.getOrDefault("userId", ""));
        if (!"ADMIN".equals(role) && !teamOwner.equals(userId)) {
            throw new RuntimeException("Unauthorized: team does not belong to you");
        }

        Map<String, Object> pkg = fetchPackage(dto.getPackageId());
        if (pkg.isEmpty()) throw new RuntimeException("Arena package not found");
        String status = String.valueOf(pkg.getOrDefault("status", "INACTIVE"));
        if (!"ACTIVE".equalsIgnoreCase(status)) {
            throw new RuntimeException("Selected arena package is not available");
        }

        // SRS US11: Blocked slots shall not be selectable by customers during booking.
        if (isSlotBlocked(dto.getPackageId(), dto.getBookingDate(), dto.getTimeSlot())) {
            throw new RuntimeException("Selected time slot is blocked and cannot be booked");
        }

        if (repository.existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
                dto.getPackageId(), dto.getBookingDate(), dto.getTimeSlot(), ACTIVE_STATUSES)) {
            throw new RuntimeException("Selected time slot is already booked");
        }

        Map<String, Object> profile = fetchProfile(userId);

        Booking b = new Booking();
        b.setUserId(userId);
        b.setCustomerName(String.valueOf(profile.getOrDefault("fullName", "Customer")));
        b.setTeamId(dto.getTeamId());
        b.setTeamName(String.valueOf(team.getOrDefault("teamName", "")));
        b.setPackageId(dto.getPackageId());
        b.setPackageName(String.valueOf(pkg.getOrDefault("name", "")));
        b.setBookingDate(dto.getBookingDate());
        b.setTimeSlot(dto.getTimeSlot());
        b.setSpecialRequest(dto.getSpecialRequest());
        b.setStatus(BookingStatus.PENDING);

        Object priceObj = pkg.get("pricePerHour");
        if (priceObj != null) b.setTotalAmount(new BigDecimal(priceObj.toString()));

        long seq = repository.countByUserId(userId) + 1;
        int numericId = extractNumericPart(userId);
        b.setReferenceNumber(String.format("ARN-%d%04d", numericId, seq));

        return mapper.map(repository.save(b), BookingDTO.class);
    }

    private int extractNumericPart(String userId) {
        StringBuilder digits = new StringBuilder();
        for (char c : userId.toCharArray()) if (Character.isDigit(c)) digits.append(c);
        return digits.length() == 0 ? 0 : Integer.parseInt(digits.toString());
    }

    public BookingDTO updateStatus(String role, Long id, BookingStatus newStatus) {
        if (!"ADMIN".equals(role)) throw new RuntimeException("Unauthorized: admin only");
        Booking b = repository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        Set<BookingStatus> allowed = TRANSITIONS.getOrDefault(b.getStatus(), Set.of());
        if (!allowed.contains(newStatus)) {
            throw new RuntimeException("Invalid status transition: " + b.getStatus() + " -> " + newStatus);
        }
        b.setStatus(newStatus);
        return mapper.map(repository.save(b), BookingDTO.class);
    }

    public BookingDTO cancel(String userId, String role, Long id) {
        Booking b = repository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"ADMIN".equals(role) && !b.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to cancel this booking");
        }
        if (b.getStatus() != BookingStatus.PENDING) {
            throw new RuntimeException("Only PENDING bookings can be cancelled");
        }
        b.setStatus(BookingStatus.CANCELLED);
        return mapper.map(repository.save(b), BookingDTO.class);
    }

    public BookingDTO getById(String userId, String role, Long id) {
        Booking b = repository.findById(id).orElseThrow(() -> new RuntimeException("Booking not found"));
        if (!"ADMIN".equals(role) && !b.getUserId().equals(userId)) {
            throw new RuntimeException("Unauthorized to view this booking");
        }
        return mapper.map(b, BookingDTO.class);
    }

    public Page<BookingDTO> search(String userId, String role, String reference, String customerName,
                                   BookingStatus status, LocalDate date,
                                   int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        String scopedUserId = "ADMIN".equals(role) ? null : userId;
        String ref = (reference != null && !reference.isBlank()) ? reference : null;
        String name = (customerName != null && !customerName.isBlank()) ? customerName : null;
        Page<Booking> result = repository.search(scopedUserId, ref, name, status, date, pageable);
        return result.map(b -> mapper.map(b, BookingDTO.class));
    }

    public DashboardSummaryDTO dashboard(String userId, String role) {
        DashboardSummaryDTO d = new DashboardSummaryDTO();
        Map<String, Long> byStatus = new LinkedHashMap<>();
        if ("ADMIN".equals(role)) {
            d.setTotalCustomers(fetchLongOrZero("http://user-service/api/users/stats", "totalCustomers", role));
            d.setTotalCategories(fetchLongOrZero("http://catalog-service/api/categories/count", "totalCategories", role));
            d.setTotalPackages(fetchLongOrZero("http://catalog-service/api/arena-packages/count", "totalActivePackages", role));
            d.setTotalBookings(repository.count());
            // System-wide status counts for administrators.
            for (BookingStatus s : BookingStatus.values()) byStatus.put(s.name(), repository.countByStatus(s));
            // SRS US09: Administrator shall be able to view recent booking activities.
            d.setRecentBookings(repository.findTop5ByOrderByCreatedAtDesc().stream()
                    .map(b -> mapper.map(b, BookingDTO.class)).toList());
        } else {
            d.setTotalTeams(fetchLongOrZero("http://user-service/api/teams/count", "totalTeams", role, userId));
            d.setTotalBookings(repository.countByUserId(userId));
            d.setActiveBookings(repository.countByUserIdAndStatus(userId, BookingStatus.CONFIRMED)
                    + repository.countByUserIdAndStatus(userId, BookingStatus.CHECKED_IN)
                    + repository.countByUserIdAndStatus(userId, BookingStatus.PENDING));
            d.setCompletedBookings(repository.countByUserIdAndStatus(userId, BookingStatus.COMPLETED));
            // SRS US09: Customers shall only see their OWN booking status counts.
            for (BookingStatus s : BookingStatus.values()) byStatus.put(s.name(), repository.countByUserIdAndStatus(userId, s));
        }
        d.setBookingsByStatus(byStatus);
        return d;
    }

    public boolean teamHasActive(Long teamId) {
        return repository.existsByTeamIdAndStatusIn(teamId, ACTIVE_STATUSES);
    }

    public boolean packageHasActive(Long packageId) {
        return repository.existsByPackageIdAndStatusIn(packageId, ACTIVE_STATUSES);
    }

    // SRS US11: a time slot already held by a non-cancelled booking.
    public boolean slotHasActiveBooking(Long packageId, LocalDate date, String timeSlot) {
        return repository.existsByPackageIdAndBookingDateAndTimeSlotAndStatusIn(
                packageId, date, timeSlot, NON_CANCELLED_STATUSES);
    }

    // Slots that are held by a customer booking (not cancelled) for a package/date,
    // exposed as { timeSlot, status } so both the admin slot page and the customer
    // booking form can show them as unavailable. No customer identity is exposed.
    public List<Map<String, String>> bookedSlots(Long packageId, LocalDate date) {
        return repository.findByPackageIdAndBookingDateAndStatusIn(packageId, date, NON_CANCELLED_STATUSES)
                .stream()
                .map(b -> Map.of("timeSlot", b.getTimeSlot(), "status", b.getStatus().name()))
                .toList();
    }

    // ── outbound calls with fallbacks ─────────────────────────────────────

    @CircuitBreaker(name = "userService", fallbackMethod = "fetchTeamFallback")
    public Map<String, Object> fetchTeam(Long teamId, String userId, String role) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-User-Id", userId);
        h.set("X-User-Role", role);
        return call("http://user-service/api/teams/" + teamId, h);
    }
    @SuppressWarnings("unused")
    private Map<String, Object> fetchTeamFallback(Long teamId, String userId, String role, Throwable ex) {
        return Collections.emptyMap();
    }

    @CircuitBreaker(name = "catalogService", fallbackMethod = "fetchPackageFallback")
    public Map<String, Object> fetchPackage(Long packageId) {
        return call("http://catalog-service/api/arena-packages/" + packageId, new HttpHeaders());
    }
    @SuppressWarnings("unused")
    private Map<String, Object> fetchPackageFallback(Long packageId, Throwable ex) {
        return Collections.emptyMap();
    }

    // SRS US11: check whether a slot is blocked by the administrator (catalog-service).
    @CircuitBreaker(name = "catalogService", fallbackMethod = "isSlotBlockedFallback")
    public boolean isSlotBlocked(Long packageId, LocalDate date, String timeSlot) {
        String url = "http://catalog-service/api/slots/is-blocked?packageId=" + packageId
                + "&date=" + date + "&timeSlot=" + timeSlot;
        Map<String, Object> body = call(url, new HttpHeaders());
        return Boolean.TRUE.equals(body.get("blocked"));
    }
    @SuppressWarnings("unused")
    private boolean isSlotBlockedFallback(Long packageId, LocalDate date, String timeSlot, Throwable ex) {
        // Fail-safe: if catalog-service is unreachable, do not block the booking on a false positive.
        return false;
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "fetchProfileFallback")
    public Map<String, Object> fetchProfile(String userId) {
        HttpHeaders h = new HttpHeaders();
        h.set("X-User-Id", userId);
        h.set("X-User-Role", "CUSTOMER");
        return call("http://user-service/api/users/me", h);
    }
    @SuppressWarnings("unused")
    private Map<String, Object> fetchProfileFallback(String userId, Throwable ex) {
        return Map.of("fullName", "Customer");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> call(String url, HttpHeaders headers) {
        HttpEntity<Void> entity = new HttpEntity<>(headers);
        Map<String, Object> body = restTemplate.exchange(url, org.springframework.http.HttpMethod.GET, 
            entity, Map.class).getBody();
        return body == null ? Collections.emptyMap() : body;
    }

    private long fetchLongOrZero(String url, String field, String role) {
        return fetchLongOrZero(url, field, role, null);
    }

    private long fetchLongOrZero(String url, String field, String role, String asUser) {
        try {
            HttpHeaders h = new HttpHeaders();
            h.set("X-User-Role", role);
            if (asUser != null) h.set("X-User-Id", asUser);
            Map<String, Object> body = call(url, h);
            Object v = body.get(field);
            if (v == null) return 0L;
            return Long.parseLong(v.toString());
        } catch (Exception ex) {
            return 0L;
        }
    }
}