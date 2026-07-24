package com.example.urlshortener.repository;

import com.example.urlshortener.model.DemoUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DemoUserRepository extends JpaRepository<DemoUser, String> {

    Optional<DemoUser> findByUsername(String username);

    boolean existsByUsername(String username);
}
