package com.example.urlshortener.service;

import com.example.urlshortener.config.AppProperties;
import com.example.urlshortener.dto.LinkResponse;
import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.ShortCodeGenerationException;
import com.example.urlshortener.model.Link;
import com.example.urlshortener.repository.LinkRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
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
     * Map an entity to its client-facing response, joining the configured base
     * address with the Short Code.
     */
    public LinkResponse toResponse(Link link) {
        return LinkResponse.from(link, appProperties.getBaseAddress());
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
