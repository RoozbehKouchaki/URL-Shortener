package com.example.urlshortener.controller;

import com.example.urlshortener.service.LinkService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

/** Public redirect: 302 to the Long URL, or 404 when unknown or inactive. */
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
