package com.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Binds the {@code app.*} configuration. {@code baseAddress} is joined with a
 * Short Code to build the Short URL, so it is configurable rather than hardcoded.
 */
@Component
@ConfigurationProperties(prefix = "app")
public class AppProperties {

    private String baseAddress;

    public String getBaseAddress() {
        return baseAddress;
    }

    public void setBaseAddress(String baseAddress) {
        this.baseAddress = baseAddress;
    }
}
