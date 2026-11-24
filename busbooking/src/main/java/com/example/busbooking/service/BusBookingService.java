package com.example.busbooking.service;

import com.example.busbooking.dto.BusBookingDTO;
import java.time.LocalDate;
import java.util.List;

/** Business logic contract for managing bus ticket bookings. */
public interface BusBookingService {

    BusBookingDTO addBooking(BusBookingDTO dto);

    List<BusBookingDTO> getAllBookings();

    BusBookingDTO getBookingById(Integer id);

    BusBookingDTO updateBooking(Integer id, BusBookingDTO dto);

    /** Cancels a booking by deleting its row. */
    void cancelBooking(Integer id);

    List<BusBookingDTO> getRouteReport(String source, String dest);

    List<BusBookingDTO> getDateReport(LocalDate travelDate);

    List<BusBookingDTO> getPassengerReport(String passengerName);
}
