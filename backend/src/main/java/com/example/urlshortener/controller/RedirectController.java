package com.example.urlshortener.controller;

import com.example.urlshortener.service.LinkService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Optional;

/**
 * Public redirect endpoint. Resolves a Short Code to its Long URL and issues a
 * 302, or returns 404 when the code is unknown or its Link is inactive.
 */
@RestController
public class RedirectController {

    private static final Logger log = LoggerFactory.getLogger(RedirectController.class);

    private final LinkService linkService;

    public RedirectController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/{shortCode:[A-Za-z0-9]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        Optional<String> target = linkService.resolveActiveTarget(shortCode);
        if (target.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        // Recording the click must never prevent the redirect from being served.
        try {
            linkService.recordClick(shortCode);
        } catch (Exception e) {
            log.warn("Failed to record click for short code '{}'; serving redirect anyway.", shortCode, e);
        }

        return ResponseEntity.status(HttpStatus.FOUND)
                .location(URI.create(target.get()))
                .build();
    }
}
