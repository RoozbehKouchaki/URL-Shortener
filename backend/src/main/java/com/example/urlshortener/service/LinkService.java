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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class LinkService {

    private static final Logger log = LoggerFactory.getLogger(LinkService.class);

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

    /** Each call produces a fresh Short Code, even for a Long URL already stored. */
    @Transactional
    public LinkResponse create(String longUrl, String ownerUsername) {
        String normalizedUrl = longUrl.trim();
        validateUrl(normalizedUrl);

        String shortCode = generateUniqueShortCode();
        Link saved = linkRepository.save(new Link(shortCode, normalizedUrl, ownerUsername));

        return toResponse(saved);
    }

    /** Empty unless the Link exists and is active. */
    @Transactional(readOnly = true)
    public Optional<String> resolveActiveTarget(String shortCode) {
        return linkRepository.findByShortCode(shortCode)
                .filter(Link::isActive)
                .map(Link::getLongUrl);
    }

    /**
     * Entry point for the public redirect path. Click recording is best-effort, so
     * a failure to record never prevents the redirect from being served.
     */
    public Optional<String> resolveTargetAndRecordClick(String shortCode) {
        Optional<String> target = resolveActiveTarget(shortCode);
        target.ifPresent(ignored -> recordClickBestEffort(shortCode));
        return target;
    }

    @Transactional
    public void recordClick(String shortCode) {
        linkRepository.incrementClickCount(shortCode);
    }

    private void recordClickBestEffort(String shortCode) {
        try {
            recordClick(shortCode);
        } catch (RuntimeException e) {
            log.warn("Failed to record click for short code '{}'; serving redirect anyway.", shortCode, e);
        }
    }

    @Transactional(readOnly = true)
    public List<LinkResponse> listMine(String ownerUsername) {
        return linkRepository.findByOwnerUsernameOrderByCreatedAtDesc(ownerUsername).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Idempotent, and never deletes: the Link and its Click Count are kept. */
    @Transactional
    public LinkResponse deactivate(String shortCode, String requestingUser) {
        Link link = findOwnedLink(shortCode, requestingUser);
        link.setActive(false);
        return toResponse(linkRepository.save(link));
    }

    @Transactional(readOnly = true)
    public LinkStatsResponse stats(String shortCode, String requestingUser) {
        Link link = findOwnedLink(shortCode, requestingUser);
        return new LinkStatsResponse(link.getShortCode(), link.getClickCount());
    }

    public LinkResponse toResponse(Link link) {
        return LinkResponse.from(link, appProperties.getBaseAddress());
    }

    /** Rejects an unknown code (404) and a non-owner (403). */
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
