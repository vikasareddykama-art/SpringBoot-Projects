package com.example.busbooking.controller;

import com.example.busbooking.dto.BusBookingDTO;
import com.example.busbooking.service.BusBookingService;
import jakarta.validation.Valid;
import java.net.URI;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

/**
 * Layer 1 - presentation. Handles HTTP only: reads the request, calls the service,
 * returns the right status code. No business rules live here.
 *
 * Base URL: /api/v1/bus-bookings (the PDF's endpoint table spells it /busbookings,
 * so that spelling is accepted too).
 */
@RestController
@RequestMapping({"/api/v1/bus-bookings", "/api/v1/busbookings"})
@CrossOrigin
public class BusBookingController {

    private final BusBookingService service;

    public BusBookingController(BusBookingService service) {
        this.service = service;
    }

    /** Add Booking - 201 Created (+ Location header). */
    @PostMapping
    public ResponseEntity<BusBookingDTO> addBooking(@Valid @RequestBody BusBookingDTO dto) {
        BusBookingDTO saved = service.addBooking(dto);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.getBookingId()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    /** Display Bookings - 200 OK. */
    @GetMapping
    public ResponseEntity<List<BusBookingDTO>> getAllBookings() {
        return ResponseEntity.ok(service.getAllBookings());
    }

    /** Get Booking by ID - 200 OK, or 404 Not Found. */
    @GetMapping("/{id}")
    public ResponseEntity<BusBookingDTO> getBookingById(@PathVariable("id") Integer id) {
        return ResponseEntity.ok(service.getBookingById(id));
    }

    /** Update Booking - 200 OK, 400 Bad Request, or 404 Not Found. */
    @PutMapping("/{id}")
    public ResponseEntity<BusBookingDTO> updateBooking(@PathVariable("id") Integer id,
                                                       @Valid @RequestBody BusBookingDTO dto) {
        return ResponseEntity.ok(service.updateBooking(id, dto));
    }

    /** Cancel Booking - 204 No Content (empty body), or 404 Not Found. */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable("id") Integer id) {
        service.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    // ------------------------------------------------------------- Reports

    /** GET /reports/route?source=Mysore&dest=Bangalore */
    @GetMapping("/reports/route")
    public ResponseEntity<List<BusBookingDTO>> routeReport(@RequestParam("source") String source,
                                                           @RequestParam("dest") String dest) {
        return ResponseEntity.ok(service.getRouteReport(source, dest));
    }

    /** GET /reports/date?travelDate=2026-12-10 */
    @GetMapping("/reports/date")
    public ResponseEntity<List<BusBookingDTO>> dateReport(
            @RequestParam("travelDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate travelDate) {
        return ResponseEntity.ok(service.getDateReport(travelDate));
    }

    /** GET /reports/passenger?passengerName=Rahul */
    @GetMapping("/reports/passenger")
    public ResponseEntity<List<BusBookingDTO>> passengerReport(
            @RequestParam("passengerName") String passengerName) {
        return ResponseEntity.ok(service.getPassengerReport(passengerName));
    }
}
