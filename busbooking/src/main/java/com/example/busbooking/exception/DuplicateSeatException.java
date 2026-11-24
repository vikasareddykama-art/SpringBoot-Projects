package com.example.busbooking.exception;

/** Thrown when the same seat is booked twice on the same bus and travel date. */
public class DuplicateSeatException extends InvalidBookingException {

    public DuplicateSeatException(String message) {
        super(message);
    }
}
