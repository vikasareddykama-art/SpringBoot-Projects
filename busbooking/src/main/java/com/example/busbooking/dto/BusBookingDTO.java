package com.example.busbooking.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Data Transfer Object for a booking. The JSON keys used by js/app.js are exactly
 * these field names. Field-level rules are enforced by Bean Validation when the
 * controller receives the request body with @Valid; cross-field rules
 * (source != destination, duplicate seat) are enforced in the service layer.
 */
public class BusBookingDTO {

    private Integer bookingId;

    @NotBlank(message = "Passenger name must not be empty")
    @Size(max = 100, message = "Passenger name must be at most 100 characters")
    private String passengerName;

    @NotBlank(message = "Bus number must not be empty")
    @Size(max = 20, message = "Bus number must be at most 20 characters")
    private String busNumber;

    @NotBlank(message = "Source must not be empty")
    @Size(max = 50, message = "Source must be at most 50 characters")
    private String source;

    @NotBlank(message = "Destination must not be empty")
    @Size(max = 50, message = "Destination must be at most 50 characters")
    private String destination;

    @NotNull(message = "Travel date is required")
    @FutureOrPresent(message = "Travel date must be today or a future date")
    private LocalDate travelDate;

    @NotNull(message = "Seat number is required")
    @Min(value = 1, message = "Seat number must be between 1 and 50")
    @Max(value = 50, message = "Seat number must be between 1 and 50")
    private Integer seatNumber;

    @NotNull(message = "Ticket fare is required")
    @DecimalMin(value = "0.00", inclusive = false, message = "Ticket fare must be greater than zero")
    @Digits(integer = 8, fraction = 2, message = "Ticket fare can have at most 8 digits and 2 decimal places")
    private BigDecimal ticketFare;

    public BusBookingDTO() {
    }

    public BusBookingDTO(Integer bookingId, String passengerName, String busNumber, String source,
                         String destination, LocalDate travelDate, Integer seatNumber,
                         BigDecimal ticketFare) {
        this.bookingId = bookingId;
        this.passengerName = passengerName;
        this.busNumber = busNumber;
        this.source = source;
        this.destination = destination;
        this.travelDate = travelDate;
        this.seatNumber = seatNumber;
        this.ticketFare = ticketFare;
    }

    public Integer getBookingId() {
        return bookingId;
    }

    public void setBookingId(Integer bookingId) {
        this.bookingId = bookingId;
    }

    public String getPassengerName() {
        return passengerName;
    }

    public void setPassengerName(String passengerName) {
        this.passengerName = passengerName;
    }

    public String getBusNumber() {
        return busNumber;
    }

    public void setBusNumber(String busNumber) {
        this.busNumber = busNumber;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public LocalDate getTravelDate() {
        return travelDate;
    }

    public void setTravelDate(LocalDate travelDate) {
        this.travelDate = travelDate;
    }

    public Integer getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(Integer seatNumber) {
        this.seatNumber = seatNumber;
    }

    public BigDecimal getTicketFare() {
        return ticketFare;
    }

    public void setTicketFare(BigDecimal ticketFare) {
        this.ticketFare = ticketFare;
    }
}
