package com.dmt.backend.common.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(
            ApiException exception,
            HttpServletRequest request) {

        log.warn(
                "Handled API exception path={} status={} message={}",
                request.getRequestURI(),
                exception.getStatus().value(),
                exception.getMessage()
        );

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                exception.getStatus().value(),
                exception.getStatus().getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(exception.getStatus())
                .body(response);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        log.warn(
                "Access denied path={} message={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        ApiErrorResponse response = new ApiErrorResponse(
                Instant.now(),
                HttpStatus.FORBIDDEN.value(),
                HttpStatus.FORBIDDEN.getReasonPhrase(),
                exception.getMessage(),
                request.getRequestURI(),
                null
        );

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleValidation(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .collect(Collectors.toMap(
                        FieldError::getField,
                        DefaultMessageSourceResolvable::getDefaultMessage,
                        (first, second) -> first
                ));

        log.warn(
                "Validation failed path={} errors={}",
                request.getRequestURI(),
                errors
        );

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation failed",
                request.getRequestURI(),
                errors
        );
    }

    /**
     * Thrown by Spring Data's deleteById()/getById() when the row does not exist.
     * Without this handler it falls through to handleUnexpected() as a 500.
     */
    @ExceptionHandler(EmptyResultDataAccessException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ApiErrorResponse handleEmptyResult(
            EmptyResultDataAccessException exception,
            HttpServletRequest request) {

        log.warn(
                "Entity not found path={} message={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                "The requested resource was not found",
                request.getRequestURI(),
                null
        );
    }

    /**
     * Thrown when a write violates a DB constraint - most commonly a foreign-key
     * constraint (e.g. deleting a screen that still has columns/filters/procedures
     * pointing at it) or a unique-key violation that slipped past application checks.
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ApiErrorResponse handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {

        log.warn(
                "Data integrity violation path={} message={}",
                request.getRequestURI(),
                exception.getMessage()
        );

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                "The operation could not be completed because related data still exists or a uniqueness constraint was violated.",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public ApiErrorResponse handleUnexpected(
            Exception exception,
            HttpServletRequest request) {

        log.error(
                "Unhandled exception path={} message={}",
                request.getRequestURI(),
                exception.getMessage(),
                exception
        );

        return new ApiErrorResponse(
                Instant.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase(),
                "Unexpected server error",
                request.getRequestURI(),
                null
        );
    }

    @ExceptionHandler(InvalidFilterException.class)
    public ResponseEntity<ApiErrorResponse> handleInvalidFilter(
            InvalidFilterException ex,
            HttpServletRequest request) {

        log.warn(
                "Invalid filter path={} message={}",
                request.getRequestURI(),
                ex.getMessage()
        );

        return ResponseEntity
                .badRequest()
                .body(ApiErrorResponse.builder()
                        .timestamp(Instant.now())
                        .status(400)
                        .error("Bad Request")
                        .message(ex.getMessage())
                        .path(request.getRequestURI())
                        .build());
    }
}