package com.example.urlshortener.controller;

import com.example.urlshortener.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/**
 * Public redirect endpoint. Resolves a Short Code to its Long URL and issues a
 * 302, or returns 404 when the code is unknown or its Link is inactive.
 *
 * <p>All logic (resolving the active target and recording the click) lives in
 * {@link LinkService}; this controller only maps the outcome to a response.
 */
@RestController
public class RedirectController {

    private final LinkService linkService;

    public RedirectController(LinkService linkService) {
        this.linkService = linkService;
    }

    @GetMapping("/{shortCode:[A-Za-z0-9]{7}}")
    public ResponseEntity<Void> redirect(@PathVariable String shortCode) {
        return linkService.resolveTargetAndRecordClick(shortCode)
                .map(target -> ResponseEntity.status(HttpStatus.FOUND)
                        .location(URI.create(target))
                        .<Void>build())
                .orElseGet(() -> ResponseEntity.notFound().<Void>build());
    }
}
