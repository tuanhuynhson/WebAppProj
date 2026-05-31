# Concert Booking Web App

This is a Spring Boot web application for a concert site. It includes a public home page, customer login and registration, a customer dashboard, ticket buying with a visual seat-section selector, merch ordering, and a small admin area for concert and seat management.

The project uses Spring MVC, Thymeleaf, Spring Data JPA, Spring Security, MySQL, and Maven.

## Main Features

- Public concert landing page.
- Customer registration and login.
- Session-based customer dashboard.
- Admin dashboard.
- Concert location management.
- Seat inventory management.
- Ticket buying flow with a visual seating layout.
- Ticket payment confirmation page.
- Merch page with cart and checkout.
- User-owned ticket and merch history on the dashboard.
- Database seeding for ticket sections and seats.

## Tech Stack

- Java 17
- Spring Boot 4.0.6
- Spring MVC
- Spring Data JPA
- Spring Security
- Thymeleaf
- MySQL
- Maven
- HTML, CSS, and JavaScript

## Project Structure

```text
webapp/
├── pom.xml
├── src/
│   ├── main/
│   │   ├── java/com/example/webapp/
│   │   │   ├── WebappApplication.java
│   │   │   ├── config/
│   │   │   ├── controller/
│   │   │   ├── dao/
│   │   │   ├── model/
│   │   │   └── service/
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── schema-auth.sql
│   │       ├── static/
│   │       │   ├── css/
│   │       │   └── js/
│   │       └── templates/
│   └── test/
└── README.md
```

## Important Routes

| Route | Purpose |
| --- | --- |
| `/` | Public home page |
| `/login` | Customer/admin login page |
| `/register` | Customer registration page |
| `/logout` | Ends the current session |
| `/dashboard` | Customer dashboard |
| `/customer/dashboard` | Alternate customer dashboard route |
| `/tickets?locationId={id}` | Ticket seat-selection page for a concert location |
| `/tickets/payment` | Ticket payment summary page |
| `/tickets/checkout` | Final ticket checkout action |
| `/merch` | Merch shop |
| `/merch/payment` | Merch payment/checkout action |
| `/admin` | Admin dashboard |
| `/admin/dashboard` | Alternate admin dashboard route |
| `/admin/concerts` | Manage concert locations |
| `/admin/seats` | Manage seat inventory |

## Running The Project

### 1. Requirements

Install:

- Java 17
- Maven
- MySQL

### 2. Database

Create a MySQL database:

```sql
CREATE DATABASE concert_booking;
```

The app is configured in `src/main/resources/application.properties`.

Important settings:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/concert_booking?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh
spring.datasource.username=root
spring.jpa.hibernate.ddl-auto=update
spring.sql.init.mode=never
server.port=8080
```

The database password is also stored in `application.properties` in this local project. For a real deployment, move database credentials into environment variables or an untracked local config file.

Because `spring.jpa.hibernate.ddl-auto=update` is enabled, Hibernate updates tables from the Java models. Because `spring.sql.init.mode=never`, `schema-auth.sql` is not automatically run on startup; it is mainly a reference/manual helper unless that setting is changed.

### 3. Start The App

```bash
mvn spring-boot:run
```

Open:

```text
http://localhost:8080/
```

### 4. Run Tests

```bash
mvn -q test
```

## Demo Account

The application creates a demo admin user on startup if it does not already exist:

```text
Email: admin@gmail.com
Password: admin
```

This is created in `WebappApplication.java` through:

```java
userService.createAdminIfMissing("admin@gmail.com", "admin");
```

Customer accounts can be created through `/register`.

## Authentication And Sessions

Authentication is handled mostly by the app's own controllers and services, not by Spring Security's default login page.

Main files:

- `SecurityConfig.java`
- `LoginController.java`
- `UserService.java`
- `User.java`
- `UserRole.java`

`SecurityConfig.java` disables Spring's default form login and permits routes at the Spring Security layer. The app then checks login state and roles manually inside controllers.

When a user logs in successfully, `LoginController` stores these values in the HTTP session:

```text
currentUserId
currentUserFullName
currentUserEmail
currentUserRole
```

Those session values are how the app knows who is currently logged in.

Password checking is done in `UserService.authenticate(...)`. Passwords are stored as BCrypt hashes, not plain text.

## User Roles

The app supports two roles:

```text
ADMIN
CUSTOMER
```

Admins can access the admin dashboard and management pages.

Customers can access the customer dashboard, buy tickets, and buy merch.

## Customer Dashboard

The customer dashboard is controlled by `CustomerDashboardController.java`.

Routes:

```text
/dashboard
/customer/dashboard
```

The dashboard shows:

- A greeting with the current user's name.
- `MY TICKET`, showing tickets owned by the logged-in user.
- `MY MERCH`, showing merch orders owned by the logged-in user.

The dashboard does not show every ticket or every merch order. It filters by the logged-in user's session id.

Ticket ownership comes from:

```text
seats.user_id
```

Merch ownership comes from:

```text
merch_order.user_id
```

## Admin Area

The admin area is controlled by `AdminController.java`.

Important admin pages:

- `admin.html`
- `manage-concerts.html`
- `manage-seats.html`

Admin features include:

- Viewing dashboard counts.
- Adding concert locations.
- Deleting concert locations.
- Viewing and updating seat status.

Dashboard seat counts use valid seat records connected to real ticket sections. This avoids counting older invalid/legacy rows that may still exist in the local database.

## Ticket System Overview

The ticket system has two connected ideas:

1. `seat_sections` describes the visual blocks shown on the ticket page.
2. `seats` contains the actual sellable ticket inventory.

This means the visual section is not itself the purchased ticket. A section is a group, and each group contains many actual seats/tickets.

Example:

```text
seat_sections
-------------
id: 12
location_id: 3
section_code: A2
ticket_type: Standing
price: 120.00
seat_location: Center floor
grid_row: 1
grid_column: 2
row_span: 2
column_span: 1
capacity: 220

seats
-----
id: 301
location_id: 3
section_id: 12
user_id: null
row_label: A2
seat_number: 1
status: AVAILABLE
```

When the customer buys from section `A2`, the app assigns available rows from `seats` where `section_id = 12`.

## Seat Visualizer

The seat visualizer is on `tickets.html`.

It uses a CSS grid with visual blocks created from database rows in `seat_sections`.

Each section has layout fields:

| Field | Meaning |
| --- | --- |
| `grid_row` | Starting row in the visual grid |
| `grid_column` | Starting column in the visual grid |
| `row_span` | How many rows the block stretches vertically |
| `column_span` | How many columns the block stretches horizontally |

This is what allows one concert location to have a normal 3x3 layout while another has larger standing areas that span multiple boxes.

For example:

```text
A2 with row_span = 2 and column_span = 1
```

means the visual block starts at `A2` and stretches vertically over two grid rows.

```text
B1 with row_span = 1 and column_span = 3
```

means the visual block stretches horizontally across three columns.

That is how a standing pit, balcony, VIP area, or wide center-floor section can be shown differently for each location.

## How Ticket Sections Render From SQL

The flow is:

1. SQL data exists in `seat_sections`.
2. `SeatSectionDao` loads sections for the selected concert location.
3. `TicketController` converts each `SeatSection` into a view object for Thymeleaf.
4. `tickets.html` loops through those sections.
5. Each section becomes a clickable visual block in the CSS grid.
6. JavaScript reads the section's `data-*` attributes when the customer clicks it.
7. The ticket summary updates on the page.
8. A hidden form input stores the selected section ids and quantities.
9. The payment page recalculates totals from the database.
10. Checkout assigns real rows in `seats` to the logged-in user.

Important Thymeleaf example:

```html
th:classappend="' seat-zone-' + ${section.modifier}"
```

This adds a CSS class based on the section data. For example, if `section.modifier` is `vip`, Thymeleaf renders:

```html
class="... seat-zone-vip"
```

That lets CSS style VIP, standing, economy, or balcony sections differently.

Another important example:

```html
th:attr="data-section-id=${section.code},
         data-section-db-id=${section.id},
         data-ticket-type=${section.ticketType},
         data-price=${section.price},
         data-price-amount=${section.priceAmount},
         data-seat-location=${section.seatLocation},
         data-availability=${section.availabilityLabel},
         data-available-count=${section.availableSeats}"
```

This writes database-backed values into HTML `data-*` attributes. JavaScript can then read them without another server request.

## Ticket Summary

The visible ticket summary starts with one selected section panel in the HTML.

When the user selects more ticket sections, JavaScript updates the summary. It does not require the server to re-render the whole page.

The JavaScript keeps the selected ticket sections in memory, updates the visible list, and writes a compact payload into a hidden input.

The submitted payload looks like:

```text
44:2,45:1
```

Meaning:

```text
section id 44, quantity 2
section id 45, quantity 1
```

The server reads this during payment and checkout.

## Ticket Payment And Seat Ownership

Ticket payment is controlled by `TicketController.java`.

The important routes are:

```text
POST /tickets/payment
POST /tickets/checkout
```

The payment page does not blindly trust the browser's total. The server reloads each selected section from the database and calculates the real total from stored prices.

During checkout:

1. The controller checks the logged-in user from the session.
2. It parses the selected section ids and quantities.
3. It finds available seats for each selected section.
4. It assigns each selected seat to the current user.
5. It changes the seat status to `BOOKED`.
6. It saves the seats.
7. It redirects the user to the dashboard.

The ownership assignment is:

```java
seat.setUserId(userId);
seat.setStatus(SeatStatus.BOOKED);
```

So the system knows a ticket belongs to a user because `seats.user_id` matches the logged-in user's id.

## TicketSectionSeeder

`TicketSectionSeeder.java` is a startup seeder.

It implements Spring Boot's `CommandLineRunner`, so this method runs automatically when the app starts:

```java
public void run(String... args)
```

The `String... args` part means the method can receive any command-line arguments passed to the app. In this project, those arguments are not the important part. The important part is that Spring Boot calls `run(...)` after the application context is ready.

The seeder does two main jobs:

1. Creates or updates visual ticket sections in `seat_sections`.
2. Creates missing actual seat inventory rows in `seats`.

It only works if concert locations already exist. It loads locations from `concert_locations`, then creates a different layout for each one.

The layouts include different span patterns, such as:

- Vertical standing areas.
- Wider front-row sections.
- Center-floor sections.
- Side pit sections.
- Split balcony sections.

This is why different ticket pages can have different visual layouts.

## Merch System

The merch system is controlled by `MerchController.java`.

Main route:

```text
/merch
```

Checkout route:

```text
POST /merch/payment
```

The merch page has a cart on the frontend. On checkout, the app saves a `MerchOrder` row with:

- `user_id`
- customer name
- email
- address
- total amount
- payment method
- cart summary
- order status
- created time

The customer dashboard loads merch orders by the logged-in user's id, so each customer only sees their own merch orders.

## Main Database Tables

### `users`

Stores login accounts.

Important fields:

- `id`
- `full_name`
- `email`
- `username`
- `password_hash`
- `role`

### `concert_locations`

Stores concert stops/venues.

Important fields:

- `id`
- `city`
- `venue`
- `concert_date`

### `seat_sections`

Stores visual ticket sections and pricing.

Important fields:

- `id`
- `location_id`
- `section_code`
- `section_name`
- `ticket_type`
- `price`
- `seat_location`
- `grid_row`
- `grid_column`
- `row_span`
- `column_span`
- `display_order`
- `capacity`

### `seats`

Stores actual sellable ticket inventory.

Important fields:

- `id`
- `location_id`
- `section_id`
- `user_id`
- `row_label`
- `seat_number`
- `status`

`section_id` connects a seat to `seat_sections.id`.

`user_id` is how the app records ticket ownership after purchase.

### `merch_order`

Stores completed merch orders.

Important fields:

- `id`
- `user_id`
- `customer_name`
- `email`
- `address`
- `total_amount`
- `payment_method`
- `cart_summary`
- `status`
- `created_at`

## Styling

The project uses a concert poster-like visual style:

- High contrast black, white, and orange.
- Large editorial type.
- Libre Caslon Display-style typography.
- Oval/circle layout motifs.
- Minimal button styling.

Important files:

- `src/main/resources/static/css/home.css`
- `src/main/resources/static/css/login.css`
- `src/main/resources/static/css/register.css`
- `src/main/resources/static/css/merch.css`

Page templates are in:

```text
src/main/resources/templates/
```

## Notes And Caveats

- Ticket payment is a demo flow. It does not connect to a real payment provider.
- Spring Security is present, but route protection is mostly done manually in controllers through session checks.
- CSRF is disabled in the current security config.
- Database credentials are currently local project config and should be moved before deployment.
- `schema-auth.sql` is not automatically applied because `spring.sql.init.mode=never`.
- Some older local databases may contain legacy invalid seat rows. Current DAO queries ignore invalid seat sections where required.
- Seat ownership currently uses a `user_id` column on `seats`; it is not modeled as a full JPA relationship to `User`.

## Common Development Commands

Start the app:

```bash
mvn spring-boot:run
```

Run tests:

```bash
mvn -q test
```

Open the app:

```text
http://localhost:8080/
```

## High-Level User Flow

### Customer

1. Register or log in.
2. Go to the home page.
3. Choose a concert location.
4. Select ticket sections and quantities.
5. Continue to ticket payment.
6. Complete checkout.
7. View owned tickets on the dashboard.
8. Shop merch.
9. View owned merch orders on the dashboard.

### Admin

1. Log in with the demo admin account.
2. Open the admin dashboard.
3. Manage concert locations.
4. Manage seat inventory.
5. Review dashboard counts.

