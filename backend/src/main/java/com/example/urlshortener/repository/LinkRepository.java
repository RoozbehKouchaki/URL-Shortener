package com.example.urlshortener.repository;

import com.example.urlshortener.model.Link;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface LinkRepository extends JpaRepository<Link, Long> {

    Optional<Link> findByShortCode(String shortCode);

    boolean existsByShortCode(String shortCode);

    List<Link> findByOwnerUsernameOrderByCreatedAtDesc(String ownerUsername);

    /** Single SQL update, so concurrent redirects cannot lose increments. */
    @Modifying
    @Transactional
    @Query("update Link l set l.clickCount = l.clickCount + 1 where l.shortCode = :shortCode")
    int incrementClickCount(@Param("shortCode") String shortCode);
}
