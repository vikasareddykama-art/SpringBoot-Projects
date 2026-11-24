package com.example.busbooking.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Maps one row of the BusBookings table.
 *
 * The unique constraint (BusNumber, TravelDate, SeatNumber) is a database-level safety net
 * for the "no duplicate seat on the same bus and date" rule; the service layer checks the
 * same rule first so the user gets a friendly message.
 */
@Entity
@Table(name = "BusBookings",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_bus_date_seat",
                columnNames = {"BusNumber", "TravelDate", "SeatNumber"}))
public class BusBooking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "BookingID")
    private Integer bookingId;

    @NotBlank
    @Size(max = 100)
    @Column(name = "PassengerName", nullable = false, length = 100)
    private String passengerName;

    @NotBlank
    @Size(max = 20)
    @Column(name = "BusNumber", nullable = false, length = 20)
    private String busNumber;

    @NotBlank
    @Size(max = 50)
    @Column(name = "Source", nullable = false, length = 50)
    private String source;

    @NotBlank
    @Size(max = 50)
    @Column(name = "Destination", nullable = false, length = 50)
    private String destination;

    @NotNull
    @Column(name = "TravelDate", nullable = false)
    private LocalDate travelDate;

    @NotNull
    @Min(1)
    @Max(50)
    @Column(name = "SeatNumber", nullable = false)
    private Integer seatNumber;

    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Column(name = "TicketFare", nullable = false, precision = 10, scale = 2)
    private BigDecimal ticketFare;

    public BusBooking() {
    }

    public BusBooking(String passengerName, String busNumber, String source, String destination,
                      LocalDate travelDate, Integer seatNumber, BigDecimal ticketFare) {
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
