-- =====================================================================
-- Bus Travel Booking Management System - MySQL setup (optional)
-- The application can create all of this itself (ddl-auto=update and
-- createDatabaseIfNotExist=true). Run this script only if you prefer
-- to create the database manually or want to show the schema.
--
--   mysql -u root -p < database/bus_booking_db.sql
-- =====================================================================

CREATE DATABASE IF NOT EXISTS bus_booking_db
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE bus_booking_db;

CREATE TABLE IF NOT EXISTS BusBookings (
    `BookingID`     INT            NOT NULL AUTO_INCREMENT,
    `PassengerName` VARCHAR(100)   NOT NULL,
    `BusNumber`     VARCHAR(20)    NOT NULL,
    `Source`        VARCHAR(50)    NOT NULL,
    `Destination`   VARCHAR(50)    NOT NULL,
    `TravelDate`    DATE           NOT NULL,
    `SeatNumber`    INT            NOT NULL,
    `TicketFare`    DECIMAL(10,2)  NOT NULL,
    PRIMARY KEY (`BookingID`),
    -- Same seat cannot be booked twice on the same bus and date
    CONSTRAINT uk_bus_date_seat UNIQUE (`BusNumber`, `TravelDate`, `SeatNumber`)
);

-- ---------------------------------------------------------------------
-- Sample data (dates are relative to the day you run the script)
-- Bus KA-05-9090 reuses seat 12 on the same day as KA-09-1234: allowed,
-- because different buses may have the same seat number.
-- ---------------------------------------------------------------------
INSERT INTO BusBookings (`PassengerName`, `BusNumber`, `Source`, `Destination`, `TravelDate`, `SeatNumber`, `TicketFare`) VALUES
    ('Rahul',          'KA-09-1234', 'Mysore',     'Bangalore',  CURDATE() + INTERVAL 2 DAY,  12,  250.00),
    ('Priya Nair',     'KA-09-1234', 'Mysore',     'Bangalore',  CURDATE() + INTERVAL 2 DAY,  13,  250.00),
    ('Rahul Verma',    'KA-01-5678', 'Bangalore',  'Chennai',    CURDATE() + INTERVAL 5 DAY,   7,  650.00),
    ('Ananya Rao',     'TS-09-4321', 'Hyderabad',  'Bangalore',  CURDATE() + INTERVAL 7 DAY,  21,  900.00),
    ('Kiran Kumar',    'KA-05-9090', 'Bangalore',  'Mysore',     CURDATE() + INTERVAL 2 DAY,  12,  260.00),
    ('Sneha Reddy',    'TS-07-1111', 'Hyderabad',  'Vijayawada', CURDATE() + INTERVAL 10 DAY, 18,  480.00),
    ('Mohammed Irfan', 'KA-09-1234', 'Mysore',     'Bangalore',  CURDATE(),                    5,  250.00),
    ('Divya Menon',    'KL-07-2468', 'Bangalore',  'Kochi',      CURDATE() + INTERVAL 4 DAY,  33, 1100.00);

-- ---------------------------------------------------------------------
-- Handy verification queries (match the three report endpoints)
-- ---------------------------------------------------------------------
-- SELECT * FROM BusBookings;
-- SELECT * FROM BusBookings WHERE LOWER(Source) = 'mysore' AND LOWER(Destination) = 'bangalore';
-- SELECT * FROM BusBookings WHERE TravelDate = CURDATE();
-- SELECT * FROM BusBookings WHERE LOWER(PassengerName) = 'rahul';
