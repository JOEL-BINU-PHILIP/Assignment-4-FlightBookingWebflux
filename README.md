
# Flight Booking WebFlux

Reactive Flight Inventory, Search, and Booking system built using **Spring Boot WebFlux**, **Reactive MongoDB**, **Reactor**, **JUnit**, **SonarCloud**, and **JMeter**.

---

## Overview

This project implements a fully reactive flight booking backend with:

* Non-blocking I/O using WebFlux and Netty
* Reactive MongoDB CRUD operations
* REST APIs for airlines, flights, and bookings
* Validation and custom API exceptions
* Unit tests with JUnit 5 and Mockito
* Load testing using JMeter
* SonarCloud static code analysis
* Swagger/OpenAPI documentation

---

## Features

### Reactive Architecture

* Built with Spring WebFlux
* Uses Reactor’s `Mono` and `Flux`
* Netty event-loop based concurrency

### Modules

* **Airline Management** – Create and list airlines
* **Flight Inventory** – Add and validate flights
* **Flight Search** – Search flights based on source, destination, and date
* **Booking System** – Book flights and generate PNR
* **Exception Handling** – Centralized GlobalErrorHandler

---

## Technology Stack

| Component         | Technology                     |
| ----------------- | ------------------------------ |
| Backend Framework | Spring Boot WebFlux            |
| Reactive Engine   | Reactor (Mono/Flux)            |
| Database          | MongoDB Reactive Driver        |
| API Documentation | SpringDoc OpenAPI (Swagger UI) |
| Testing           | JUnit 5, Mockito, StepVerifier |
| Code Quality      | SonarCloud                     |
| Load Testing      | Apache JMeter                  |
| Build & CI        | Maven, GitHub Actions          |

---

## Project Structure

```
src/
 ├── main/java/com.flightapp
 │    ├── controller
 │    ├── service
 │    ├── service/impl
 │    ├── repository
 │    ├── dto
 │    ├── entity
 │    ├── exception
 │    ├── config
 │    └── util
 └── test/java/com.flightapp
      ├── controller
      ├── service
      ├── repository
      └── TestDataFactory
```

---

## API Documentation (Swagger UI)

Once the application is running, Swagger UI is available at:

```
http://localhost:8080/swagger-ui.html
```

or

```
http://localhost:8080/swagger-ui/index.html
```

---

## Running the Application

### 1. Clone the Repository

```bash
git clone <repo-url>
cd FlightBookingWebFlux
```

### 2. Start MongoDB

Ensure MongoDB is running on:

```
mongodb://localhost:27017
```

### 3. Build and Run

```bash
mvn clean install
mvn spring-boot:run
```

Service runs at:

```
http://localhost:8080
```

---

## Running Tests

### Unit Tests + Coverage

```bash
mvn clean verify
```

### Jacoco Report

Generated at:

```
target/site/jacoco/index.html
```

---

## SonarCloud Integration

This project uses:

* `sonar.projectKey`
* `sonar.organization`
* `sonar.host.url`
* `sonar.coverage.jacoco.xmlReportPaths`

GitHub Actions workflow generates:

* Tests
* Coverage reports
* SonarCloud scan

---

## Load Testing with JMeter

JMeter scenarios tested:

* 20 users
* 50 users
* 100 users
---

## Configuration

Application properties:

```
src/main/resources/application.properties
```

Contains:

* MongoDB config
* Server port
* Logging settings


---
