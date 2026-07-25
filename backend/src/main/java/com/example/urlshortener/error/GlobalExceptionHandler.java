package com.example.urlshortener.error;

import com.example.urlshortener.exception.InvalidUrlException;
import com.example.urlshortener.exception.LinkNotFoundException;
import com.example.urlshortener.exception.NotLinkOwnerException;
import com.example.urlshortener.exception.ShortCodeGenerationException;
import com.example.urlshortener.exception.UsernameTakenException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Translates exceptions into {@link ErrorResponse}. Bodies carry a caller-safe
 * message only; stack traces and database details are never exposed.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

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

    /**
     * Sign-up with an existing username -> 409. This does reveal that the username
     * exists, which is unavoidable for a username the caller must choose.
     */
    @ExceptionHandler(UsernameTakenException.class)
    public ResponseEntity<ErrorResponse> handleUsernameTaken(UsernameTakenException ex) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), "username");
    }

    @ExceptionHandler(NotLinkOwnerException.class)
    public ResponseEntity<ErrorResponse> handleNotOwner(NotLinkOwnerException ex) {
        return build(HttpStatus.FORBIDDEN, ex.getMessage(), null);
    }

    /**
     * Failed sign-in -> 401. The message does not distinguish an unknown username
     * from a wrong password, which would allow account enumeration.
     */
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationFailure(AuthenticationException ex) {
        log.warn("Failed sign-in attempt: {}", ex.getMessage());
        return build(HttpStatus.UNAUTHORIZED, "Invalid username or password.", null);
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

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body.", null);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex) {
        return build(HttpStatus.METHOD_NOT_ALLOWED, "HTTP method not supported for this endpoint.", null);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex) {
        return build(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Unsupported or missing Content-Type.", null);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResource(NoResourceFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "Resource not found.", null);
    }

    /** Fallback: generic message to the caller, details logged only. */
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
