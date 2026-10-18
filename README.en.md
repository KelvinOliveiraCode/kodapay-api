
# kodapay-api

![language: Java](https://img.shields.io/badge/language-Java-blue)   ![status: done](https://img.shields.io/badge/status-done-green)   ![license: MIT](https://img.shields.io/badge/license-MIT-blue)

## Summary

REST API in Java 21 with Spring Boot 3.5.16, developed as a software architecture study. Payment REST API with 6 GoF patterns

## Problem / why it exists

In small applications, business rules, validation, persistence and notification tend to get mixed into a single service layer. This produces conditionals that are hard to test and change: adding a payment method or a risk rule requires editing existing code. The project isolates each responsibility in the design pattern that fits it, so the main flow does not need to know the details of each subsystem.

## Architecture

The flow follows a layered pipeline:

1. Controller receives the HTTP request, validates the payload and delegates to the service.
2. Service acts as a facade: picks strategies, executes validation chains, persists and publishes events.
3. Strategies isolate variable calculation (e.g. fee per payment method).
4. Chain of Responsibility runs validation rules in fixed order; a veto interrupts the rest.
5. Observer publishes events to decoupled listeners (messaging, metrics) without the main flow knowing them.
6. Singleton registry keeps transactions in memory in the JVM.

Monetary operations use `BigDecimal` with `HALF_EVEN` rounding to two decimal places, never `double`.

## Technologies

- Java 21
- Spring Boot 3.5.16 (Web, Validation)
- Maven
- springdoc-openapi (Swagger UI / OpenAPI 3)
- JUnit 5, Mockito, MockMvc
- GitHub Actions (CI)

Direct dependencies from `pom.xml`:
- kodapay-api
- spring-boot-starter-web
- spring-boot-starter-validation
- spring-boot-starter-test
- springdoc-openapi-starter-webmvc-ui


## What it does

Payment REST API with 6 GoF patterns

## How to run

Requirements: Java 21 and Maven.

```
./mvnw verify     # build and run the tests
./mvnw spring-boot:run
# API at http://localhost:8080
```

If `application.yml` exists, adjust the port before running.

## Project structure

- .github/ (1 arquivo(s))
- .mvn/ (1 arquivo(s))
- src/ (1 arquivo(s))
- .gitignore, LICENSE, mvnw, mvnw.cmd, pom.xml, README.en.md, README.md


## Trade-offs and decisions

- New chain per request: keeping the chain as a singleton bean made risk rules stop executing after the first request. The factory now creates a new chain per payment.
- `BigDecimal` for money, never `double`, with `HALF_EVEN` to two decimal places.
- Versioned API and errors in RFC 7807 (ProblemDetail) format.
- No database: the registry keeps transactions in memory to keep focus on patterns; the repository interface leaves the path open to swap in JPA without touching the service.
- No external gateway: it is an honest simulation, no real payment happens.
- Messaging and metrics simulated in memory, exposed only through the observability endpoint.

## Known limitations

- In-memory history: restarting the application erases registered transactions.
- No authentication or authorization.
- Messaging and metrics do not integrate external providers.
- Fixed lists and rules in code (e.g. blocklist, velocity limit).

## Tests

```
./mvnw test
```

The repository contains automated tests.

## License

Distributed under the MIT License. See the `LICENSE` file in this repository.

## Author

Kelvin Oliveira, https://github.com/KelvinOliveiraCode
Portfolio: https://kelvinoliveiracode.github.io/portfolio/
