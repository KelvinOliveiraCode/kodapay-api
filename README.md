# kodapay-api — API de pagamentos com 6 padrões GoF em um domínio real

![Java](https://img.shields.io/badge/Java-21-ED8B00?style=flat-square&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.5-6DB33F?style=flat-square&logo=springboot&logoColor=white)
![Maven](https://img.shields.io/badge/Maven-4-C71A36?style=flat-square&logo=apachemaven&logoColor=white)
![Tests](https://img.shields.io/badge/tests-26%20verdes-brightgreen?style=flat-square)
![CI](https://img.shields.io/github/actions/workflows/status/KelvinOliveiraCode/kodapay-api/ci.yml?branch=main&style=flat-square&label=CI)
![License](https://img.shields.io/badge/license-MIT-yellow?style=flat-square)

API REST de processamento de pagamentos em **Java 21 + Spring Boot 3.5**, construída do zero para o desafio de Padrões de Projeto da DIO — mas não como cópia do lab: o domínio (taxas por método, antifraude em cadeia, auditoria) é novo, e cada padrão GoF aparece porque resolve um problema real do fluxo de pagamento, não como demonstração de vitrine. 26 testes (JUnit 5, Mockito, MockMvc) rodam no CI a cada push.

---

## 🇧🇷 Português

### Sobre

O KodaPay processa pagamentos com política de taxas por método (Strategy), cadeia de risco antifraude (Chain of Responsibility), registro único de transações (Singleton/Repository), coordenação simples para o controller (Facade) e reação desacoplada a eventos de pagamento (Observer). A API é versionada (`/api/v1`), documentada com OpenAPI 3 (Swagger UI em `/docs`) e valida payloads com Bean Validation.

### Os padrões e onde moram

| Padrão | Arquivo | O que resolve aqui |
|---|---|---|
| **Strategy** | `strategy/FeeCalculator` + 4 implementações | Cada método de pagamento tem sua política de taxa — o service pergunta "qual é a taxa?" sem conhecer as fórmulas. Novo método = nova classe `@Component`, zero mudança em código existente (Open/Closed). |
| **Chain of Responsibility** | `chain/RiskHandler` → `HighValue`, `Blocklist`, `Velocity` | Cada regra de risco avalia e passa adiante — ou veta com `422`. A ordem fica nas anotações `@Order`, não dentro das regras. |
| **Singleton** | `chain/PaymentRegistry` | Um registro de transações por JVM, com a trilha usada pela regra de velocidade. |
| **Facade** | `service/PaymentService.process()` | O controller chama um método; taxa, risco, persistência e eventos são coordenados por dentro. |
| **Observer** | `observer/PaymentEvent` + `MessagingListener`, `MetricsListener` | O processamento publica um evento; mensageria e métricas reagem sem o fluxo principal conhecê-las — visível em `GET /api/v1/observability`. |
| **Repository** | `PaymentRegistry` (interface de repositório) | Persistência em memória para manter o foco nos padrões; a interface deixa o caminho pronto para trocar por JPA sem tocar no service. |

### Pipeline de um pagamento

```
POST /api/v1/payments
        │
        ▼
[Strategy] escolhe a política de taxa pelo método
        │  PIX/BOLETO: taxa fixa | CREDIT_CARD: 3,99% | DEBIT_CARD: 1,89%
        ▼
[Chain of Responsibility] risco, elo a elo
        │  1. valor > R$ 15.000? → 422
        │  2. pagador na lista restrita? → 422
        │  3. 3+ transações na última hora? → 422
        ▼
[Singleton/Repository] grava a transação
        │
        ▼
[Observer] publica evento → mensageria + métricas reagem
        │
        ▼
201 + trilha de auditoria completa
```

### Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/payments` | Processa um pagamento (retorna taxa, total e trilha de auditoria) |
| GET | `/api/v1/payments` | Histórico de transações |
| GET | `/api/v1/payments/{id}` | Detalhe de uma transação |
| GET | `/api/v1/observability` | Efeitos acumulados dos observadores |
| GET | `/docs` · `/api-docs` | Swagger UI · especificação OpenAPI 3 |

Exemplo:

```bash
curl -X POST http://localhost:9090/api/v1/payments \
  -H "Content-Type: application/json" \
  -d '{"payerName":"Kelvin Oliveira","description":"Assinatura plano Pro","amount":37.50,"method":"PIX"}'
```

Resposta `201` (trecho): pagamento aprovado com `fee: 0.49`, `total: 37.99`, `status: APPROVED` e `auditTrail` documentando as três checagens de risco aprovadas. Erros: `400` payload inválido, `404` transação inexistente, `422` veto de risco (motivo no `detail`).

### Taxas aplicadas

| Método | Política |
|---|---|
| PIX | fixa R$ 0,49 |
| BOLETO | fixa R$ 2,49 |
| CREDIT_CARD | 3,99% do valor |
| DEBIT_CARD | 1,89% do valor |

### Decisões de design

- **Cadeia por requisição.** A primeira versão tinha a cadeia de risco como bean singleton — a segunda requisição HTTP encontrava o ponteiro no fim da cadeia e passava por todas as checagens. O bug foi pego por teste de integração (esperava `422`, voltava `201`) e resolvido com um factory que cria cadeia nova a cada pagamento. É o tipo de defeito que passa em review e estoura em produção.
- **`BigDecimal` para dinheiro** com arredondamento `HALF_EVEN` de 2 casas — nunca `double`.
- **API versionada** (`/api/v1`) e erros no formato RFC 7807 (ProblemDetail).
- **Sem gateway externo.** As taxas são reais de mercado, mas nenhum pagamento acontece — uma simulação honesta.

### Como rodar

```bash
./mvnw verify            # build + 26 testes
./mvnw spring-boot:run   # API em http://localhost:9090 — Swagger em /docs
```

### Autor

**Kelvin Oliveira** — [GitHub](https://github.com/KelvinOliveiraCode) · [LinkedIn](https://www.linkedin.com/in/kelvin-oliveira-0282033b4/)

---

## 🇺🇸 English

A payment processing REST API in **Java 21 + Spring Boot 3.5**, built for the DIO design-pattern challenge — not as a copy of the lab, but with a fresh domain (per-method fees, fraud chain, audit trail) where each GoF pattern exists because it solves a real problem in the payment flow. 26 tests (JUnit 5, Mockito, MockMvc) run in CI on every push.

### Where the patterns live

| Pattern | Files | What it solves here |
|---|---|---|
| **Strategy** | `strategy/FeeCalculator` + 4 impls | Each payment method owns its fee policy — the service asks "what's the fee?" without knowing formulas. New method = new `@Component`, zero changes to existing code (Open/Closed). |
| **Chain of Responsibility** | `chain/RiskHandler` → `HighValue`, `Blocklist`, `Velocity` | Each risk rule evaluates and passes along — or vetoes with `422`. Ordering lives in `@Order` annotations, not inside the rules. |
| **Singleton** | `PaymentRegistry` | One transaction registry per JVM, feeding the velocity rule. |
| **Facade** | `PaymentService.process()` | The controller calls one method; fees, risk, persistence and events are coordinated inside. |
| **Observer** | `PaymentEventPublisher` + `MessagingListener`, `MetricsListener` | Processing publishes an event; messaging and metrics react without the main flow knowing them — visible at `GET /api/v1/observability`. |
| **Repository** | `PaymentRegistry` interface | In-memory persistence to keep focus on patterns; the interface is the ready path to swap in JPA without touching the service. |

### Design decisions worth reading

- **Per-request chain.** The first version had the risk chain as a singleton bean — the second HTTP request found the pointer at the end of the chain and skipped all risk checks. Caught by an integration test (expected `422`, got `201`) and fixed with a factory that builds a fresh chain per payment. The kind of defect that passes review and blows up in production.
- **`BigDecimal` for money**, `HALF_EVEN` rounding to 2 decimal places — never `double`.
- **Versioned API** (`/api/v1`), RFC 7807 ProblemDetail errors.
- **No external gateway.** Fees mirror real market rates, but no payment is processed — an honest simulation.

### Run it

```bash
./mvnw verify            # build + 26 tests
./mvnw spring-boot:run   # API at http://localhost:9090 — Swagger at /docs
```

### Author

**Kelvin Oliveira** — [GitHub](https://github.com/KelvinOliveiraCode) · [LinkedIn](https://www.linkedin.com/in/kelvin-oliveira-0282033b4/)
