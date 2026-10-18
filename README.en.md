# kodapay-api

[![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)](https://adoptium.net/)   ![status: done](https://img.shields.io/badge/status-done-green)   ![license: MIT](https://img.shields.io/badge/license-MIT-blue)

## Summary

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21 | Payment REST API with 6 GoF patterns
## Problem / Why it exists

The project addresses a practical scope and automates a task that, done manually, is repetitive and error-prone. The focus is on turning data or processes into reliable output, with readable, testable, and extensible code.
## Architecture

The code is organized by responsibility into 0 main classes:
## What it does

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21 | Payment REST API with 6 GoF patterns
## How to run

Requirements: Java 21 and Maven.

```bash
./mvnw verify     # build and run tests
./mvnw spring-boot:run
# API at http://localhost:8080
```
If `application.yml` exists, adjust the port before running.
## Project Structure (Key Classes)


## Trade-offs and Technical Decisions

- New chain per request: keeping the chain as a singleton bean made risk rules stop executing after the first request. The factory now creates a new chain per payment.
- `BigDecimal` for money, never `double`, with `HALF_EVEN` rounding to two decimal places.
- Versioned API and errors in RFC 7807 (ProblemDetail) format.
- No database: the registry keeps transactions in memory to keep focus on patterns; the repository interface leaves the path open to swap in JPA without touching the service.
- No external gateway: it is an honest simulation, no real payment happens.
- Messaging and metrics simulated in memory, exposed only through the observability endpoint.
## Known Limitations

- In-memory history: restarting the application erases registered transactions.
- No authentication or authorization.
- Messaging and metrics do not integrate external providers (Kafka, Prometheus, etc.).
- Blocklist and velocity limits fixed in code (not configurable).
## Tests

```bash
./mvnw test
```
The repository contains automated tests (JUnit 5, Mockito, MockMvc).
## License

Distributed under the MIT License. See the `LICENSE` file in this repository.
## Author

Kelvin Oliveira ,  https://github.com/KelvinOliveiraCode
Portfolio: https://kelvinoliveiracode.github.io/portfolio/
