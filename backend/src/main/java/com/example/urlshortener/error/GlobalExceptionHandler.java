package com.example.urlshortener.error;

import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.LinkNotFoundException;
import com.example.urlshortener.exception.NotLinkOwnerException;
import com.example.urlshortener.exception.ShortCodeGenerationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into a consistent {@link ErrorResponse}. Response
 * bodies carry a caller-safe message only; stack traces, internal class names,
 * and database details are never exposed.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    /**
     * Bean Validation failure on a request body -> 400, reporting the first
     * offending field.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldErrors().stream().findFirst().orElse(null);
        String field = fieldError != null ? fieldError.getField() : null;
        String message = fieldError != null ? fieldError.getDefaultMessage() : "Invalid request.";

        return build(HttpStatus.BAD_REQUEST, message, field);
    }

    @ExceptionHandler(InvalidUrlException.class)
    public ResponseEntity<ErrorResponse> handleInvalidUrl(InvalidUrlException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), "longUrl");
    }

    @ExceptionHandler(NotLinkOwnerException.class)
    public ResponseEntity<ErrorResponse> handleNotOwner(NotLinkOwnerException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    @ExceptionHandler(LinkNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(LinkNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(ShortCodeGenerationException.class)
    public ResponseEntity<ErrorResponse> handleGenerationExhausted(ShortCodeGenerationException ex) {
        log.error("Short code generation exhausted", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Could not generate a unique short code. Please try again.", null);
    }

    /**
     * Malformed or unreadable request body (for example invalid JSON) -> 400.
     * These are caller mistakes, so no error-level stack trace is logged.
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body.", null);
    }

    /**
     * Wrong HTTP method for the path -> 405.
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint.", null);
    }

    /**
     * Missing or unsupported Content-Type -> 415.
     */
    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported or missing Content-Type.", null);
    }

    /**
     * No handler/resource for the requested path -> 404.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource not found.", null);
    }

    /**
     * Anything unforeseen -> 500 with a generic message, details logged only.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleUnexpected(Exception ex) {
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred.", null);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, String field) {
        ErrorResponse body = field != null
                ? ErrorResponse.of(status.value(), message, field)
                : ErrorResponse.of(status.value(), message);
        return ResponseEntity.status(status).body(body);
    }
}
