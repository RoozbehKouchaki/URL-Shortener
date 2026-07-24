package com.example.urlshortener.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/** Binds {@code app.*}. {@code baseAddress} is joined with a Short Code to build a Short URL. */
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
