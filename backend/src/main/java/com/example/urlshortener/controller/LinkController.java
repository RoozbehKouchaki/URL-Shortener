package com.example.urlshortener.controller;

import com.example.urlshortener.dto.CreateLinkRequest;
import com.example.urlshortener.dto.LinkResponse;
import com.example.urlshortener.service.LinkService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;
import java.util.List;

/** Management endpoints. The owner comes from {@code Principal}; logic lives in the service. */
@RestController
@RequestMapping("/api/links")
public class LinkController {

    private final LinkService linkService;

    public LinkController(LinkService linkService) {
        this.linkService = linkService;
    }

    @PostMapping
    public ResponseEntity<LinkResponse> create(@Valid @RequestBody CreateLinkRequest request,
                                               Principal principal) {
        LinkResponse response = linkService.create(request.longUrl(), principal.getName());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<LinkResponse> listMine(Principal principal) {
        return linkService.listMine(principal.getName());
    }

    @PostMapping("/{shortCode}/deactivate")
    public LinkResponse deactivate(@PathVariable String shortCode, Principal principal) {
        return linkService.deactivate(shortCode, principal.getName());
    }

    /** Includes {@code clickCount}, so no separate stats endpoint is needed. */
    @GetMapping("/{shortCode}")
    public LinkResponse get(@PathVariable String shortCode, Principal principal) {
        return linkService.get(shortCode, principal.getName());
    }
}
