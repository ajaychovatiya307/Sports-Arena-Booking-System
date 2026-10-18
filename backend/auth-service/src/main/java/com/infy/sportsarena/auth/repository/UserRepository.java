package com.infy.sportsarena.auth.repository;

import com.infy.sportsarena.auth.entity.UserCredentials;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserCredentials, Long> {
    Optional<UserCredentials> findByEmail(String email);
    Optional<UserCredentials> findByUserId(String userId);
    boolean existsByEmail(String email);
    long countByRole(String role);
}
