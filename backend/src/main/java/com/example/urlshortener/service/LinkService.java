package com.example.urlshortener.service;

import com.example.urlshortener.config.AppProperties;
import com.example.urlshortener.dto.LinkResponse;
import com.example.urlshortener.dto.LinkStatsResponse;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.LinkNotFoundException;
import com.example.urlshortener.exception.NotLinkOwnerException;
import com.example.urlshortener.exception.ShortCodeGenerationException;
import com.example.urlshortener.model.Link;
import com.example.urlshortener.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Set;

/**
 * Business logic for creating and managing Links.
 */
@Service
public class LinkService {

    private static final int MAX_CODE_ATTEMPTS = 5;
    private static final Set<String> LOOPBACK_HOSTS = Set.of("localhost", "127.0.0.1", "::1", "[::1]");

    private final LinkRepository linkRepository;
    private final ShortCodeGenerator shortCodeGenerator;
    private final AppProperties appProperties;

    public LinkService(LinkRepository linkRepository,
                       ShortCodeGenerator shortCodeGenerator,
                       AppProperties appProperties) {
        this.linkRepository = linkRepository;
        this.shortCodeGenerator = shortCodeGenerator;
        this.appProperties = appProperties;
    }

    /**
     * Create a new Link owned by {@code ownerUsername}. Each call produces an
     * independent Link with a fresh Short Code, even when the Long URL matches
     * an existing one.
     */
    @Transactional
    public LinkResponse create(String longUrl, String ownerUsername) {
        String normalizedUrl = longUrl.trim();
        validateUrl(normalizedUrl);

        String shortCode = generateUniqueShortCode();
        Link saved = linkRepository.save(new Link(shortCode, normalizedUrl, ownerUsername));

        return toResponse(saved);
    }

    /**
     * Return the caller's Links, newest first. Empty when the caller owns none.
     */
    @Transactional(readOnly = true)
    public List<LinkResponse> listMine(String ownerUsername) {
        return linkRepository.findByOwnerUsernameOrderByCreatedAtDesc(ownerUsername).stream()
                .map(this::toResponse)
                .toList();
    }

    /**
     * Deactivate a Link the caller owns. Idempotent: an already-inactive Link
     * stays inactive and still succeeds. The Link and its Click Count are kept,
     * never deleted.
     */
    @Transactional
    public LinkResponse deactivate(String shortCode, String requestingUser) {
        Link link = findOwnedLink(shortCode, requestingUser);
        link.setActive(false);
        return toResponse(linkRepository.save(link));
    }

    /**
     * Return the Click Count for a Link the caller owns.
     */
    @Transactional(readOnly = true)
    public LinkStatsResponse stats(String shortCode, String requestingUser) {
        Link link = findOwnedLink(shortCode, requestingUser);
        return new LinkStatsResponse(link.getShortCode(), link.getClickCount());
    }

    /**
     * Map an entity to its client-facing response, joining the configured base
     * address with the Short Code.
     */
    public LinkResponse toResponse(Link link) {
        return LinkResponse.from(link, appProperties.getBaseAddress());
    }

    /**
     * Load a Link by code, rejecting an unknown code (404) and a caller who is
     * not the owner (403).
     */
    private Link findOwnedLink(String shortCode, String requestingUser) {
        Link link = linkRepository.findByShortCode(shortCode)
                .orElseThrow(() -> new LinkNotFoundException("No link found for the given short code."));

        if (!link.getOwnerUsername().equals(requestingUser)) {
            throw new NotLinkOwnerException("You do not own this link.");
        }
        return link;
    }

    private String generateUniqueShortCode() {
        for (int attempt = 0; attempt < MAX_CODE_ATTEMPTS; attempt++) {
            String candidate = shortCodeGenerator.generate();
            if (!linkRepository.existsByShortCode(candidate)) {
                return candidate;
            }
        }
        throw new ShortCodeGenerationException(
                "Exhausted " + MAX_CODE_ATTEMPTS + " attempts to generate a unique short code.");
    }

    private void validateUrl(String url) {
        URI uri;
        try {
            uri = new URI(url);
        } catch (URISyntaxException e) {
            throw new InvalidUrlException("Long URL is not a valid address.");
        }

        String host = uri.getHost();
        if (host == null || host.isBlank()) {
            throw new InvalidUrlException("Long URL is not a valid address.");
        }

        String normalizedHost = host.toLowerCase();
        if (LOOPBACK_HOSTS.contains(normalizedHost)) {
            throw new InvalidUrlException("Loopback addresses are not allowed.");
        }

        String baseHost = baseAddressHost();
        if (baseHost != null && baseHost.equals(normalizedHost)) {
            throw new InvalidUrlException("The service's own address is not allowed.");
        }
    }

    private String baseAddressHost() {
        try {
            String host = new URI(appProperties.getBaseAddress()).getHost();
            return host != null ? host.toLowerCase() : null;
        } catch (URISyntaxException e) {
            return null;
        }
    }
}
