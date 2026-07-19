package com.example.urlshortener.repository;

import com.example.urlshortener.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA repository over the {@link Link} entity. Query
 * implementations are generated from the method names, so there is no
 * hand-written SQL to maintain.
 */
@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    /**
     * Look up a Link by its Short Code (used by the redirect and management paths).
     */
    Optional<Link> findByShortCode(String shortCode);

    /**
     * Uniqueness pre-check used by the Short Code generation retry loop.
     */
    boolean existsByShortCode(String shortCode);

    /**
     * Return a Demo User's Links, most recently created first (Requirement 3.4).
     */
    List<Link> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);
}
