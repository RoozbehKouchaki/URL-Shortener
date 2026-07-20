package com.example.urlshortener.security;

import com.example.urlshortener.error.ErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Writes the consistent {@link ErrorResponse} JSON body for a 401 when a
 * request to {@code /api/**} arrives with missing or invalid credentials.
 *
 * <p>Spring Security rejects these requests inside the filter chain, before any
 * controller or the {@code @RestControllerAdvice} runs, so the entry point must
 * format the response itself (Requirements 6.6, 6.7, 7.4).
 *
 * <p>It deliberately omits the {@code WWW-Authenticate} header so the browser
 * does not show its native Basic-auth popup over the Angular login form.
 */
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    public RestAuthenticationEntryPoint(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);

        ErrorResponse body = ErrorResponse.of(
                HttpServletResponse.SC_UNAUTHORIZED,
                "Authentication required: valid credentials must be provided.");

        objectMapper.writeValue(response.getWriter(), body);
    }
}
