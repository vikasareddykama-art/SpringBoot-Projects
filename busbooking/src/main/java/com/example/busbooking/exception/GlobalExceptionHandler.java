package com.example.busbooking.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

/**
 * Converts exceptions into JSON like:
 * { "timestamp": "...", "status": 400, "error": "Bad Request",
 *   "message": "Seat number must be between 1 and 50",
 *   "errors": { "seatNumber": "Seat number must be between 1 and 50" } }
 * js/app.js reads "message" for alerts and "errors" to highlight form fields.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNotFound(ResourceNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateSeatException.class)
    public ResponseEntity<Map<String, Object>> handleDuplicateSeat(DuplicateSeatException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        errors.put("seatNumber", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), errors);
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidBooking(InvalidBookingException ex) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), null);
    }

    /** Bean Validation failures on the @Valid request body. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errors.putIfAbsent(fieldError.getField(), fieldError.getDefaultMessage());
        }
        String message = errors.isEmpty() ? "Validation failed" : String.join("; ", errors.values());
        return build(HttpStatus.BAD_REQUEST, message, errors);
    }

    /** Malformed JSON, or a value of the wrong type (e.g. travelDate not in yyyy-MM-dd). */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, Object>> handleUnreadable(HttpMessageNotReadableException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid request body. Check the JSON syntax, use yyyy-MM-dd for travelDate, "
                        + "a whole number for seatNumber and a number for ticketFare.", null);
    }

    /** Bad path variable or query parameter, e.g. /bus-bookings/abc or ?travelDate=10-12-2026. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for '" + ex.getName() + "'. Dates must use the format yyyy-MM-dd "
                        + "and IDs must be whole numbers.", null);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParam(MissingServletRequestParameterException ex) {
        return build(HttpStatus.BAD_REQUEST,
                "Required query parameter '" + ex.getParameterName() + "' is missing.", null);
    }

    /** Safety net for database-level unique constraint hits (two users booking the same seat at once). */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrity(DataIntegrityViolationException ex) {
        log.warn("Data integrity violation: {}", ex.getMostSpecificCause().getMessage());
        return build(HttpStatus.BAD_REQUEST,
                "The booking could not be saved. That seat may already be booked on this bus and date.", null);
    }

    /**
     * Fallback. Standard Spring MVC errors (404 for unknown URLs, 405 for wrong HTTP method, ...)
     * keep their own status code; anything else is reported as 500.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleAny(Exception ex) {
        if (ex instanceof ErrorResponse errorResponse) {
            String detail = errorResponse.getBody().getDetail();
            return build(errorResponse.getStatusCode(), detail != null ? detail : "Request failed", null);
        }
        log.error("Unexpected error", ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again.", null);
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatusCode status, String message,
                                                      Map<String, String> errors) {
        HttpStatus resolved = HttpStatus.resolve(status.value());
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", resolved != null ? resolved.getReasonPhrase() : "Error");
        body.put("message", message);
        if (errors != null && !errors.isEmpty()) {
            body.put("errors", errors);
        }
        return ResponseEntity.status(status).body(body);
    }
}
