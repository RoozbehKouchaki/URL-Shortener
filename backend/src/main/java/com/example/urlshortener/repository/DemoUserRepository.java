package com.example.urlshortener.repository;

import com.example.urlshortener.model.DemoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Spring Data JPA repository over the {@link DemoUser} entity.
 */
@Repository
public interface DemoUserRepository extends JpaRepository<DemoUser, String> {

    /**
     * Load a Demo User by username (used by authentication).
     */
    Optional<DemoUser> findByUsername(String username);

    /**
     * Used by startup seeding to create an account only when it is absent.
     */
    boolean existsByUsername(String username);
}
