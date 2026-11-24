package com.example.busbooking.exception;

/** Thrown when booking data breaks a business rule. Mapped to HTTP 400. */
public class InvalidBookingException extends RuntimeException {

    public InvalidBookingException(String message) {
        super(message);
    }
}
