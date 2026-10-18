package com.infy.sportsarena.user.repository;

import com.infy.sportsarena.user.entity.CustomerProfile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerProfileRepository extends JpaRepository<CustomerProfile, Long> {
    Optional<CustomerProfile> findByUserId(String userId);
    boolean existsByUserId(String userId);
    Page<CustomerProfile> findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCaseOrUserIdContainingIgnoreCase(
            String fullName, String email, String userId, Pageable pageable);
}
