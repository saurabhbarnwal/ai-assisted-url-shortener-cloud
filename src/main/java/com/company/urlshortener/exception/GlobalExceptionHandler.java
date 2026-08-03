package com.company.urlshortener.exception;

import com.company.urlshortener.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/** Converts exceptions into structured HTTP error responses. */
@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

  /** Handles invalid URL requests. */
  @ExceptionHandler(InvalidUrlException.class)
  public ResponseEntity<ErrorResponse> handleInvalidUrl(
      InvalidUrlException exception, HttpServletRequest request) {
    return buildError(HttpStatus.BAD_REQUEST, exception.getMessage(), List.of(), request);
  }

  /** Handles missing short codes. */
  @ExceptionHandler(UrlNotFoundException.class)
  public ResponseEntity<ErrorResponse> handleUrlNotFound(
      UrlNotFoundException exception, HttpServletRequest request) {
    return buildError(HttpStatus.NOT_FOUND, exception.getMessage(), List.of(), request);
  }

  /** Handles short code generation exhaustion. */
  @ExceptionHandler(ShortCodeGenerationException.class)
  public ResponseEntity<ErrorResponse> handleShortCodeGeneration(
      ShortCodeGenerationException exception, HttpServletRequest request) {
    return buildError(HttpStatus.SERVICE_UNAVAILABLE, exception.getMessage(), List.of(), request);
  }

  /** Handles bean validation errors. */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(
      MethodArgumentNotValidException exception, HttpServletRequest request) {
    List<String> details =
        exception.getBindingResult().getFieldErrors().stream()
            .map(this::formatFieldError)
            .toList();
    return buildError(HttpStatus.BAD_REQUEST, "Request validation failed", details, request);
  }

  /** Handles unexpected errors without exposing internals to API clients. */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleUnexpected(
      Exception exception, HttpServletRequest request) {
    LOGGER.error("Unhandled API exception", exception);
    return buildError(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "An unexpected error occurred",
        List.of(),
        request);
  }

  private ResponseEntity<ErrorResponse> buildError(
      HttpStatus status, String message, List<String> details, HttpServletRequest request) {
    return ResponseEntity.status(status)
        .body(
            new ErrorResponse(
                Instant.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                details,
                request.getRequestURI()));
  }

  private String formatFieldError(FieldError fieldError) {
    return fieldError.getField() + ": " + fieldError.getDefaultMessage();
  }
}
