package com.infy.sportsarena.user.service;

import com.infy.sportsarena.user.dto.CustomerProfileDTO;
import com.infy.sportsarena.user.entity.CustomerProfile;
import com.infy.sportsarena.user.repository.CustomerProfileRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    @Autowired
    private CustomerProfileRepository repository;
    @Autowired
    private ModelMapper mapper;
    @Autowired
    private RestTemplate restTemplate;

    public CustomerProfileDTO createInternal(CustomerProfileDTO dto) {
        if (repository.existsByUserId(dto.getUserId())) {
            return mapper.map(repository.findByUserId(dto.getUserId()).get(), CustomerProfileDTO.class);
        }
        CustomerProfile e = new CustomerProfile();
        e.setUserId(dto.getUserId());
        e.setFullName(dto.getFullName());
        e.setEmail(dto.getEmail());
        return mapper.map(repository.save(e), CustomerProfileDTO.class);
    }

    public CustomerProfileDTO getByUserId(String userId) {
        CustomerProfile p = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for " + userId));
        return mapper.map(p, CustomerProfileDTO.class);
    }

    public CustomerProfileDTO updateProfile(String userId, CustomerProfileDTO dto) {
        CustomerProfile p = repository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Customer profile not found for " + userId));
        p.setFullName(dto.getFullName());
        p.setContactNumber(dto.getContactNumber());
        p.setAddress(dto.getAddress());
        CustomerProfileDTO saved = mapper.map(repository.save(p), CustomerProfileDTO.class);
        // Keep the display name in sync in auth_db so the dashboard/header reflect it.
        syncNameToAuth(userId, dto.getFullName());
        return saved;
    }

    private void syncNameToAuth(String userId, String fullName) {
        try {
            Map<String, String> body = new HashMap<>();
            body.put("userId", userId);
            body.put("fullName", fullName);
            restTemplate.put("http://auth-service/api/auth/internal/profile", body);
        } catch (Exception ex) {
            System.err
                    .println("[user-service] name sync to auth-service failed for " + userId + " : " + ex.getMessage());
        }
    }

    public Page<CustomerProfileDTO> listAll(String q, int page, int size, String sortBy, String direction) {
        Sort sort = "desc".equalsIgnoreCase(direction) ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<CustomerProfile> result;
        if (q == null || q.isBlank()) {
            result = repository.findAll(pageable);
        } else {
            result = repository
                    .findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserIdContainingIgnoreCase(q, q, q,
                            pageable);
        }
        return result.map(e -> mapper.map(e, CustomerProfileDTO.class));
    }

    public long count() {
        return repository.count();
    }
}