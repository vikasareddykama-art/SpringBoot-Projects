package com.example.busbooking.repository;

import com.example.busbooking.entity.BusBooking;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Persistence layer. Spring Data JPA generates the SQL for every method below
 * from the method name; no implementation class is needed.
 */
@Repository
public interface BusRepository extends JpaRepository<BusBooking, Integer> {

    // ---- Report queries required by the specification ----------------------

    /** Route report: bookings for a source + destination pair (case-insensitive). */
    List<BusBooking> findBySourceIgnoreCaseAndDestinationIgnoreCase(String source, String destination);

    /** Travel date report. */
    List<BusBooking> findByTravelDate(LocalDate travelDate);

    /** Passenger report (exact name, case-insensitive). */
    List<BusBooking> findByPassengerNameIgnoreCase(String passengerName);

    // ---- Duplicate seat checks used by the service layer -------------------

    /** True when the seat is already booked on that bus for that date (used when adding). */
    boolean existsByBusNumberIgnoreCaseAndTravelDateAndSeatNumber(
            String busNumber, LocalDate travelDate, Integer seatNumber);

    /** Same check, ignoring the booking being edited (used when updating). */
    boolean existsByBusNumberIgnoreCaseAndTravelDateAndSeatNumberAndBookingIdNot(
            String busNumber, LocalDate travelDate, Integer seatNumber, Integer bookingId);
}
