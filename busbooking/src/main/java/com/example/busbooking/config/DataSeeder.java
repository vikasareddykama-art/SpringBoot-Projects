package com.example.busbooking.config;

import com.example.busbooking.entity.BusBooking;
import com.example.busbooking.repository.BusRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Inserts sample bookings the first time the application starts (only when the
 * BusBookings table is empty). Dates are relative to today so the data always
 * passes the "today or future" rule. Disable with app.seed-sample-data=false.
 *
 * Note: bus KA-05-9090 reuses seat 12 on the same date as KA-09-1234 - allowed,
 * because different buses may have the same seat number.
 */
@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    private final BusRepository repository;

    @Value("${app.seed-sample-data:true}")
    private boolean seedEnabled;

    public DataSeeder(BusRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (!seedEnabled || repository.count() > 0) {
            return;
        }
        LocalDate today = LocalDate.now();
        List<BusBooking> samples = List.of(
                new BusBooking("Rahul", "KA-09-1234", "Mysore", "Bangalore", today.plusDays(2), 12, new BigDecimal("250.00")),
                new BusBooking("Priya Nair", "KA-09-1234", "Mysore", "Bangalore", today.plusDays(2), 13, new BigDecimal("250.00")),
                new BusBooking("Rahul Verma", "KA-01-5678", "Bangalore", "Chennai", today.plusDays(5), 7, new BigDecimal("650.00")),
                new BusBooking("Ananya Rao", "TS-09-4321", "Hyderabad", "Bangalore", today.plusDays(7), 21, new BigDecimal("900.00")),
                new BusBooking("Kiran Kumar", "KA-05-9090", "Bangalore", "Mysore", today.plusDays(2), 12, new BigDecimal("260.00")),
                new BusBooking("Sneha Reddy", "TS-07-1111", "Hyderabad", "Vijayawada", today.plusDays(10), 18, new BigDecimal("480.00")),
                new BusBooking("Mohammed Irfan", "KA-09-1234", "Mysore", "Bangalore", today, 5, new BigDecimal("250.00")),
                new BusBooking("Divya Menon", "KL-07-2468", "Bangalore", "Kochi", today.plusDays(4), 33, new BigDecimal("1100.00")));
        repository.saveAll(samples);
        log.info("Inserted {} sample bus bookings", samples.size());
    }
}
