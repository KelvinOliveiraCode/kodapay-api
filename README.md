# kodapay-api

<div align="center">

**English** · [Português (Brasil)](README.md)

</div>

[![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)](https://adoptium.net/) [![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-%236DB33F?style=flat-square)](https://spring.io/projects/spring-boot) [![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/) [![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5)](https://junit.org/junit5/) [![MIT License](https://img.shields.io/badge/License-MIT-f4c430?style=flat-square)](LICENSE)

### API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21

---

## O que é

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21 | Payment REST API with 6 GoF patterns

---

## Quick Start

```bash
# Clone and enter
git clone https://github.com/KelvinOliveiraCode/<repo>.git
cd <repo>

# Build and run tests
./mvnw verify

# Run the application
./mvnw spring-boot:run
# API available at http://localhost:8080
# Swagger UI at http://localhost:8080/docs
```

## Como funciona

Clean, modular structure with separation of concerns.

---

## Capabilities

| Capability | Description |
|------------|-------------|
| REST API | Spring Boot 3, versioned endpoints, RFC 7807 errors |
| Fee Calculation | Strategy pattern ,  PIX R$0.49, Boleto R$2.49, Credit 3.99%, Debit 1.89% |
| Risk Validation | Chain of Responsibility ,  HighValue -> Blocklist -> Velocity |
| Transaction Registry | Singleton in-memory registry with full audit trail |
| Event System | Observer pattern ,  decoupled messaging & metrics listeners |
| Observability | /api/v1/observability endpoint exposing listener effects |
| API Docs | springdoc-openapi ,  Swagger UI at /docs, OpenAPI 3 at /api-docs |
| Testing | JUnit 5, Mockito, MockMvc ,  integration + unit tests |


---

## Showcase

### Payment Pipeline Deep Dive

Trace a payment request through the full pipeline:

1. **Controller** validates payload -> **Service (Facade)** coordinates
2. **Strategy** selects fee calculator by payment method
3. **Chain of Responsibility** runs 3 risk rules in order (veto = stop)
4. **Singleton Registry** persists with audit trail
5. **Observer** notifies messaging & metrics listeners

[See the code](src/main/java/com/kodapay/service/PaymentService.java) · [Risk Chain](src/main/java/com/kodapay/chain/RiskChain.java)

---

## FAQ

<details>
<summary><strong>Why a new chain per request?</strong></summary>

A singleton chain would keep its internal pointer at the end after the first request, causing all subsequent risk checks to be skipped. The factory creates a fresh chain each time.

</details>

<details>
<summary><strong>Why BigDecimal and not double?</strong></summary>

Financial calculations require exact decimal representation. double introduces floating-point errors (e.g., 0.1 + 0.2 ≠ 0.3). BigDecimal with HALF_EVEN rounding guarantees cent-precision.

</details>

<details>
<summary><strong>Can I swap the in-memory registry for a database?</strong></summary>

Yes. The registry is behind an interface; replace the implementation with JPA/Hibernate without touching the service layer.

</details>

<details>
<summary><strong>Is this production-ready?</strong></summary>

It is a **study project** demonstrating GoF patterns. It lacks auth, external messaging, and persistence ,  add those for production.

</details>



---

## Documentation

- **Source Code** ,  Browse the `src/` directory for implementation details
- **API Reference** ,  Swagger UI at `/docs` when running
- **Architecture Diagram** ,  See [architecture.mermaid](docs/architecture.mermaid) (if present)
- **Changelog** ,  [CHANGELOG.md](CHANGELOG.md) (if present)
- **Contributing** ,  [CONTRIBUTING.md](CONTRIBUTING.md) (if present)

## License

[MIT](LICENSE) ,  Kelvin Oliveira

---

<div align="center">

**Built with** · Java 21 + Spring Boot 3 + Maven

**Author** · [Kelvin Oliveira](https://github.com/KelvinOliveiraCode) · [Portfolio](https://kelvinoliveiracode.github.io/portfolio/)

</div>