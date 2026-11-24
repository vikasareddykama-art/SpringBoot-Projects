# Bus Travel Booking Management System

Spring Boot REST API + MySQL + Bootstrap 5 / Vanilla JavaScript frontend (college mini-project, Project 8).

| Layer | Technology |
|---|---|
| Frontend | HTML5, CSS3, Bootstrap 5, Vanilla JavaScript, Fetch API |
| Backend | Java 17, Spring Boot 3.3.5, Spring Web, Spring Data JPA, Hibernate, Bean Validation |
| Database | MySQL 8 (`bus_booking_db`, table `BusBookings`) |

---

## 1. Prerequisites

- JDK 17 or newer
- Maven 3.9+ (or an IDE with Maven support: IntelliJ IDEA, Eclipse/STS, VS Code with Java extensions)
- MySQL 8 running on `localhost:3306`
- Internet access the first time (Maven downloads dependencies; the page loads Bootstrap from a CDN)
- Postman (for API testing)

## 2. Run the project (step by step)

**Step 1 - Database.** Make sure MySQL is running. Nothing else is required: the JDBC URL contains
`createDatabaseIfNotExist=true`, so `bus_booking_db` and the `BusBookings` table are created on first start.
If you prefer to create them manually (e.g. to show the schema), run:

```bash
mysql -u root -p < database/bus_booking_db.sql
```

**Step 2 - Credentials.** Open `src/main/resources/application.properties`. The defaults are user `root`, password `root`.
Change them in the file, or set environment variables instead:

```bash
# Linux / macOS
export DB_USERNAME=root
export DB_PASSWORD=your_password
# Windows PowerShell
$env:DB_USERNAME="root"; $env:DB_PASSWORD="your_password"
```

**Step 3 - Start the application.**

```bash
cd bus-booking-system
mvn spring-boot:run
```

(In an IDE: open the folder as a Maven project and run `BusBookingApplication`.)

**Step 4 - Open the app.** Go to <http://localhost:8080>. On first start 8 sample bookings are inserted automatically.

To start with an empty table, set `app.seed-sample-data=false` in `application.properties`.

To build a runnable jar: `mvn clean package`, then `java -jar target/busbooking-1.0.0.jar`.

---

## 3. Every file and its purpose

```
bus-booking-system/
├── pom.xml
├── README.md
├── .gitignore
├── database/
│   └── bus_booking_db.sql
├── postman/
│   └── BusBooking.postman_collection.json
└── src/main/
    ├── java/com/example/busbooking/
    │   ├── BusBookingApplication.java
    │   ├── controller/BusBookingController.java
    │   ├── service/BusBookingService.java
    │   ├── service/BusBookingServiceImpl.java
    │   ├── repository/BusRepository.java
    │   ├── entity/BusBooking.java
    │   ├── dto/BusBookingDTO.java
    │   ├── exception/ResourceNotFoundException.java
    │   ├── exception/InvalidBookingException.java
    │   ├── exception/DuplicateSeatException.java
    │   ├── exception/GlobalExceptionHandler.java
    │   └── config/DataSeeder.java
    └── resources/
        ├── application.properties
        └── static/
            ├── index.html
            ├── css/style.css
            └── js/app.js
```

| File | Purpose |
|---|---|
| `pom.xml` | Maven build: Spring Web, Spring Data JPA, Validation, MySQL driver, test starter. |
| `application.properties` | Server port, MySQL connection, Hibernate settings. Uses `PhysicalNamingStrategyStandardImpl` so columns are named exactly `BookingID`, `PassengerName`, ... as in the specification. |
| `BusBookingApplication.java` | `main()` method; starts Spring Boot. |
| **Layer 1 - Presentation** | |
| `controller/BusBookingController.java` | REST endpoints under `/api/v1/bus-bookings`. Only handles HTTP: reads input, calls the service, returns 200/201/204. |
| **Layer 2 - Business logic** | |
| `service/BusBookingService.java` | Interface listing every operation. |
| `service/BusBookingServiceImpl.java` | Implements the operations; validates all rules, checks duplicate seats, converts entity <-> DTO, runs report logic. |
| **Layer 3 - Persistence** | |
| `entity/BusBooking.java` | JPA entity mapped to table `BusBookings` (with a unique key on bus + date + seat). |
| `dto/BusBookingDTO.java` | JSON shape exchanged with the frontend, carries the Bean Validation annotations. |
| `repository/BusRepository.java` | `extends JpaRepository<BusBooking, Integer>` with the three required finders plus two `exists...` methods used for the duplicate-seat check. |
| **Layer 4 - Database** | |
| `database/bus_booking_db.sql` | Optional manual script: creates `bus_booking_db`, table `BusBookings` and sample rows. |
| **Support classes** | |
| `exception/*` | Custom exceptions (404 not found, 400 invalid booking, 400 duplicate seat) and `GlobalExceptionHandler`, which turns every error into a JSON body `{timestamp, status, error, message, errors}`. |
| `config/DataSeeder.java` | Inserts 8 sample bookings at startup when the table is empty. Dates are relative to today so they are always valid. |
| **Frontend** | |
| `static/index.html` | Page structure: navbar, signboard dashboard with statistics, search/report panel, bookings table, add/edit modal, view modal, cancel confirmation, toast area. |
| `static/css/style.css` | Theme and layout on top of Bootstrap 5. |
| `static/js/app.js` | All `fetch()` calls, client-side validation, table rendering, statistics, report filters, modals and messages. |
| `postman/BusBooking.postman_collection.json` | 15 ready-made Postman requests with automated checks. |

### Table definition (matches the specification)

| Column | Type |
|---|---|
| BookingID | INT PRIMARY KEY AUTO_INCREMENT |
| PassengerName | VARCHAR(100) NOT NULL |
| BusNumber | VARCHAR(20) NOT NULL |
| Source | VARCHAR(50) NOT NULL |
| Destination | VARCHAR(50) NOT NULL |
| TravelDate | DATE NOT NULL |
| SeatNumber | INT NOT NULL |
| TicketFare | DECIMAL(10,2) NOT NULL |

---

## 4. REST API

Base URL: `http://localhost:8080/api/v1/bus-bookings`

> The endpoint table in the second PDF spells the path `/api/v1/busbookings`, while its explanation text and the project
> prompt use `/api/v1/bus-bookings`. The controller accepts **both**; the frontend uses `bus-bookings`.

| Operation | HTTP | Endpoint | Success | Errors |
|---|---|---|---|---|
| Add booking | POST | `/api/v1/bus-bookings` | 201 + body + `Location` header | 400 |
| Display bookings | GET | `/api/v1/bus-bookings` | 200 | - |
| Get booking by ID | GET | `/api/v1/bus-bookings/{id}` | 200 | 400 (bad id), 404 |
| Update booking | PUT | `/api/v1/bus-bookings/{id}` | 200 | 400, 404 |
| Cancel booking | DELETE | `/api/v1/bus-bookings/{id}` | 204 (empty body) | 404 |
| Route report | GET | `/api/v1/bus-bookings/reports/route?source=Mysore&dest=Bangalore` | 200 | 400 |
| Travel date report | GET | `/api/v1/bus-bookings/reports/date?travelDate=2026-12-10` | 200 | 400 |
| Passenger report | GET | `/api/v1/bus-bookings/reports/passenger?passengerName=Rahul` | 200 | 400 |

Reports return a JSON array (possibly empty). The passenger report matches the full name, ignoring case (`Rahul` finds `rahul`,
not `Rahul Verma`), exactly as `findByPassengerNameIgnoreCase` in the specification.

### Example: add a booking

`POST /api/v1/bus-bookings`

```json
{
  "passengerName": "Rahul",
  "busNumber": "KA-09-9999",
  "source": "Mysore",
  "destination": "Bangalore",
  "travelDate": "2026-12-10",
  "seatNumber": 14,
  "ticketFare": 250.00
}
```

Response `201 Created`:

```json
{
  "bookingId": 9,
  "passengerName": "Rahul",
  "busNumber": "KA-09-9999",
  "source": "Mysore",
  "destination": "Bangalore",
  "travelDate": "2026-12-10",
  "seatNumber": 14,
  "ticketFare": 250.00
}
```

Use a `travelDate` that is today or later; the rules reject past dates.

### Example: error response (duplicate seat) - `400 Bad Request`

```json
{
  "timestamp": "2026-11-23T10:15:42.123",
  "status": 400,
  "error": "Bad Request",
  "message": "Seat 14 on bus KA-09-9999 is already booked for 2026-12-10. Choose a different seat.",
  "errors": { "seatNumber": "Seat 14 on bus KA-09-9999 is already booked for 2026-12-10. Choose a different seat." }
}
```

### Example: not found - `404 Not Found`

```json
{ "timestamp": "2026-11-23T10:16:01.456", "status": 404, "error": "Not Found", "message": "Booking with ID 999 was not found" }
```

---

## 5. Validation rules and where they are enforced

| # | Rule | Frontend (`app.js`) | Backend |
|---|---|---|---|
| 1 | Passenger name not empty | `validateBooking()` | `@NotBlank` on DTO + `validate()` in service |
| 2 | Bus number not empty | `validateBooking()` | `@NotBlank` + service |
| 3 | Source and destination different | compared case-insensitively | service (`InvalidBookingException`) |
| 4 | Seat number 1-50 | integer check, `min`/`max` on the input | `@Min(1) @Max(50)` + service |
| 5 | Travel date today or future | `min` on the date input + check using local date | `@FutureOrPresent` + service |
| 6 | Ticket fare > 0 | `> 0` and max 2 decimals | `@DecimalMin(inclusive=false)` + `@Digits` + service |
| 7 | No duplicate seat for same bus + date | server message shown on the Seat field | service (`existsBy...` query) **and** unique key `uk_bus_date_seat` in MySQL |

Business rules: Booking ID is auto-generated (`IDENTITY`); different buses may reuse a seat number; bus numbers are stored upper-case
and compared ignoring case; "cancel" physically deletes the row through `DELETE`.

---

## 6. Test the APIs with Postman

**Option A - import the ready-made collection (fastest)**

1. Start the application.
2. Postman -> **Import** -> select `postman/BusBooking.postman_collection.json`.
3. Right-click the collection -> **Run collection** -> **Run**.
4. All 15 requests should pass (green): add, duplicate seat, list, get by id, update, three reports, four validation failures,
   a 404, delete (204) and a 404 after deletion. The collection creates its own test booking and deletes it at the end.

**Option B - manual**

1. New request -> `POST` -> `http://localhost:8080/api/v1/bus-bookings` -> Body -> raw -> JSON -> paste the example above -> **Send** (expect 201).
2. `GET http://localhost:8080/api/v1/bus-bookings` (expect 200 and a JSON array).
3. `GET http://localhost:8080/api/v1/bus-bookings/1`
4. `PUT http://localhost:8080/api/v1/bus-bookings/1` with a modified JSON body.
5. `DELETE http://localhost:8080/api/v1/bus-bookings/9` (expect 204, empty body).
6. Reports: in the **Params** tab add `source` / `dest`, or `travelDate`, or `passengerName`.

**Equivalent curl commands**

```bash
curl -i -X POST http://localhost:8080/api/v1/bus-bookings -H "Content-Type: application/json" \
  -d '{"passengerName":"Rahul","busNumber":"KA-09-9999","source":"Mysore","destination":"Bangalore","travelDate":"2026-12-10","seatNumber":14,"ticketFare":250}'
curl -s "http://localhost:8080/api/v1/bus-bookings/reports/route?source=Mysore&dest=Bangalore"
curl -s "http://localhost:8080/api/v1/bus-bookings/reports/date?travelDate=2026-12-10"
curl -s "http://localhost:8080/api/v1/bus-bookings/reports/passenger?passengerName=Rahul"
curl -i -X DELETE http://localhost:8080/api/v1/bus-bookings/9
```

---

## 7. Using the website

1. **Dashboard** - the green signboard shows total bookings, fare collected, buses booked and passengers travelling today (all computed from MySQL data).
2. **Add booking** - opens the form. Invalid fields are highlighted with a message; server errors (e.g. duplicate seat) appear in a red alert and on the field. **Reset** clears the form (or restores the original values when editing).
3. **Find bookings** - search by route (both fields), by travel date, or by passenger name. The matching rows replace the table content, a yellow bar names the active search, and **Show all bookings** returns to the full list.
4. **Bookings table** - eye = view the ticket, pencil = edit, bin = cancel (asks for confirmation first). The table refreshes after every successful operation.

### Screenshots to capture for submission

1. Dashboard with statistics and the bookings table.
2. Add booking form, with and without validation errors.
3. Duplicate-seat error message.
4. Route, date and passenger search results.
5. Edit and cancel confirmation dialogs.
6. Postman: 201 on POST, 204 on DELETE, one report, one 400.
7. MySQL Workbench / CLI: `SELECT * FROM BusBookings;`
8. Console output showing `Started BusBookingApplication`.

---

## 8. Troubleshooting

| Problem | Fix |
|---|---|
| `Access denied for user 'root'` | Fix `DB_USERNAME` / `DB_PASSWORD` or `application.properties`. |
| `Communications link failure` | MySQL is not running or not on port 3306. |
| `Port 8080 was already in use` | Set `server.port=8081` (and open that port in the browser). |
| Page is unstyled / icons missing | The browser needs internet access for the Bootstrap CDN files. |
| Table shows "Cannot reach the server" | The Spring Boot app is not running; start it and click **Try again**. |
| Sample data missing | Table was not empty, or `app.seed-sample-data=false`. |
| `Unknown column` after changing the entity | Drop the table (or database) and restart so Hibernate recreates it. |

## 9. Suggested Git commits

```
git init
git add pom.xml src/main/resources/application.properties database && git commit -m "Project setup, MySQL config and schema"
git add src/main/java/com/example/busbooking/entity src/main/java/com/example/busbooking/dto src/main/java/com/example/busbooking/repository && git commit -m "Add entity, DTO and JPA repository"
git add src/main/java/com/example/busbooking/service src/main/java/com/example/busbooking/exception && git commit -m "Add service layer, validation and exception handling"
git add src/main/java/com/example/busbooking/controller src/main/java/com/example/busbooking/config src/main/java/com/example/busbooking/BusBookingApplication.java && git commit -m "Add REST controller and sample data"
git add src/main/resources/static && git commit -m "Add Bootstrap frontend"
git add postman README.md .gitignore && git commit -m "Add Postman collection and documentation"
```
