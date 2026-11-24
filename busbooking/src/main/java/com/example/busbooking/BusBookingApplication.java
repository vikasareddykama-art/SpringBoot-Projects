package com.example.busbooking;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point. Starts the embedded Tomcat server, connects to MySQL and serves
 * the static frontend from src/main/resources/static/.
 */
@SpringBootApplication
public class BusBookingApplication {

    public static void main(String[] args) {
        SpringApplication.run(BusBookingApplication.class, args);
    }
}
