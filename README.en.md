# KodaPay API

![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-%236DB33F?style=flat-square)
![CI](https://github.com/KelvinOliveiraCode/kodapay-api/actions/workflows/ci.yml/badge.svg)

REST payment processing API with Java 21 and Spring Boot 3 that applies GoF design patterns (Strategy, Chain of Responsibility, Observer, Factory, Singleton and Facade) across fee calculation, risk assessment, persistence, and notification in a transaction flow.

## Problem / Why it exists

Processing a payment requires coordinating responsibilities that should not live together. The fee varies by payment method, risk rules must be evaluated in fixed order, the transaction must be recorded with a full audit trail, and external systems must be notified — all without blocking the main flow. Putting this in a single service layer creates conditionals that are hard to test and change. Each responsibility here stays isolated in the pattern that fits it, so adding a new payment method or a new risk rule does not require changing existing code.

## What it does

- Exposes POST /api/v1/payments to process a payment (payer, description, amount, method).
- Calculates the fee by method: Pix R$ 0,49 fixed, boleto R$ 2,49 fixed, credit 3.99% of the amount, debit 1.89% of the amount.
- Runs three risk rules in chain: amount above R$ 15.000,00, payer on the restricted list, and more than three transactions from the same payer in the last hour.
- Rejects the request with HTTP 422 when a rule vetoes, stating the reason.
- Persists the approved transaction with the full audit trail.
- Publishes a completion event; messaging and metrics listeners react without the main flow knowing them, visible at GET /api/v1/observability.
- Queries transaction history and detail by id.
- Serves interactive documentation at GET /docs (Swagger UI) and the OpenAPI 3 specification at GET /api-docs.

## Stack

- Java 21
- Spring Boot 3.5.16 (Web, Validation)
- Maven
- springdoc-openapi 2.8.17
- JUnit 5, Mockito, MockMvc — 26 tests
- GitHub Actions (JDK 21 Temurin, mvn -B verify)

## How it works

Payment pipeline

1. PaymentController validates the payload (bean validation) and delegates to PaymentService, which acts as a facade for the subsystems.
2. PaymentService chooses the fee strategy by method (Strategy). A new payment method means a new class annotated with @Component; nothing in the service changes.
3. Risk: the factory creates a new chain per request (Factory + Chain of Responsibility). The three handlers run in the order given by @Order: amount limit, restricted list, and velocity. Any veto interrupts the rest.
4. If approved, PaymentRegistry — the single registry per JVM (Singleton) — records the transaction.
5. PaymentService publishes PaymentEvent (Observer); MessagingListener and MetricsListener react in a decoupled way.
6. Response 201 with the payment, the total (amount plus fee), and the audit trail.

All monetary calculations use BigDecimal with HALF_EVEN rounding to two decimal places.

Endpoints

| Method | Route | Description |
|---|---|---|
| POST | /api/v1/payments | Processes a payment (returns fee, total, and audit trail) |
| GET | /api/v1/payments | Transaction history |
| GET | /api/v1/payments/{id} | Transaction detail |
| GET | /api/v1/observability | Accumulated observer effects (messaging + metrics) |
| GET | /docs | Swagger UI |
| GET | /api-docs | OpenAPI 3 specification |

Request example

```bash
curl -X POST http://localhost:9090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{
    "payerName": "Kelvin Oliveira",
    "description": "Assinatura plano Pro",
    "amount": 37.50,
    "method": "PIX"
  }'
```

Response (201)

```json
{
  "payment": {
    "id": "04ac9fb6-aafe-429b-8fc5-9932bc0c27e2",
    "payerName": "Kelvin Oliveira",
    "description": "Assinatura plano Pro",
    "amount": 37.50,
    "method": "PIX",
    "fee": 0.49,
    "total": 37.99,
    "status": "APPROVED",
    "createdAt": "2026-09-06T20:56:40.490126900-03:00"
  },
  "auditTrail": [
    "[HIGH_VALUE] aprovado: valor dentro do limite",
    "[BLOCKLIST] aprovado: pagador não consta na lista restrita",
    "[VELOCITY] aprovado: 0 transação(ões) na última hora"
  ]
}
```

Possible error codes are 400 (invalid payload), 404 (nonexistent transaction), and 422 (risk veto, with the reason in the detail field, RFC 7807).

## How to run

You need Java 21 and Maven.

```bash
./mvnw verify        # build and runs the 26 tests
./mvnw spring-boot:run
# API at http://localhost:9090 — Swagger at /docs
```

## Project structure

```
src/main/java/com/kodapay/
  chain/        RiskHandler (3 handlers: amount limit, restricted list, velocity), RiskChain,
                RiskChainFactory (new chain per payment), PaymentRegistry (single registry)
  domain/       Payment, PaymentMethod, PaymentStatus, AuditEvent
  dto/          PaymentRequest, PaymentResponse, PaymentView
  exception/    InvalidPaymentException, PaymentNotFoundException, RiskViolationException
  observer/     PaymentEvent, PaymentEventPublisher, MessagingListener, MetricsListener
  service/      PaymentService (facade: fee, risk, registry, event)
  strategy/     FeeCalculator + Pix, Boleto, CreditCard, DebitCard
  web/          PaymentController, ObservabilityController, GlobalExceptionHandler
src/test/       26 tests split across chain, service, strategies, and controller
pom.xml         Java 21, Spring Boot 3.5.16, springdoc-openapi 2.8.17
src/main/resources/application.yml   port 9090, paths /docs and /api-docs
```

## Trade-offs and decisions

- New chain per request. The first version kept the chain as a singleton bean: the second request found the pointer at the end and no risk rule ran. An integration test caught the problem (expected 422, got 201). RiskChainFactory now creates a new chain per payment. This is the kind of defect that passes review and fails in production.
- BigDecimal for money, never double, with HALF_EVEN rounding to two decimal places.
- Versioned API (/api/v1) and errors in RFC 7807 format (ProblemDetail).
- No database. PaymentRegistry keeps transactions in memory to keep the focus on patterns; the repository interface leaves the path ready to switch to JPA without touching the service.
- No external gateway. Fees are market-based, but no real payment happens — it is an honest simulation.
- Messaging and metrics are simulated in memory (listeners in an ArrayList), exposed only through the observability endpoint.

## Known limitations

- In-memory history: restarting the application erases recorded transactions.
- No authentication or authorization.
- Messaging and metrics do not integrate external providers.
- The risk restricted list is a fixed set in code.
- The velocity rule counts only transactions from the running JVM instance.

## License

MIT

## Author

Kelvin Oliveira — github.com/KelvinOliveiraCode
