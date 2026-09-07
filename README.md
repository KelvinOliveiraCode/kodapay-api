# KodaPay API

API REST de processamento de pagamentos construída do zero para o desafio de **Padrões de Projeto** do bootcamp da DIO. Não é uma cópia do laboratório: o domínio (pagamentos, taxas, antifraude) é novo, e cada padrão aparece porque resolve um problema real do fluxo — não como demonstração de vitrine.

![CI](https://github.com/KelvinOliveiraCode/kodapay-api/actions/workflows/ci.yml/badge.svg)

## Stack

- Java 21, Spring Boot 3.5 (Web, Validation)
- Maven
- springdoc-openapi (Swagger UI em `/docs`)
- JUnit 5 + Mockito + MockMvc (26 testes)

## Os padrões e onde moram

| Padrão | Arquivo | O que resolve aqui |
|---|---|---|
| **Strategy** | `strategy/FeeCalculator` + 4 implementações | Cada método de pagamento (Pix, boleto, crédito, débito) tem sua política de taxa. O service pergunta "qual é a taxa?" sem conhecer as fórmulas. Novo método = nova classe anotada com `@Component`, zero mudança no código existente. |
| **Chain of Responsibility** | `chain/RiskHandler` → `HighValue`, `Blocklist`, `Velocity` | Cada regra de risco avalia a transação e passa adiante — ou veta com `422`. A ordem fica nas anotações `@Order`, não dentro das regras. |
| **Singleton** | `chain/PaymentRegistry` + escopo padrão do Spring | Um único registro de transações por JVM, com a trilha usada pela regra de velocidade. |
| **Facade** | `service/PaymentService.process()` | O controller chama um método. Taxa, risco, persistência e eventos são coordenados por dentro. |
| **Observer** | `observer/PaymentEvent` + `MessagingListener`, `MetricsListener` | O processamento publica um evento; mensageria e métricas reagem sem o fluxo principal conhecê-las. Visível em `GET /api/v1/observability`. |
| **Repository** | `chain/PaymentRegistry` | Persistência em memória com interface de repositório (sem banco para manter o foco nos padrões). |

## Pipeline de um pagamento

```
POST /api/v1/payments
        │
        ▼
[Strategy] escolhe a política de taxa pelo método
        │  Pix/boleto: taxa fixa | crédito: 3,99% | débito: 1,89%
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

## Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | `/api/v1/payments` | Processa um pagamento (retorna taxa, total e trilha de auditoria) |
| GET | `/api/v1/payments` | Histórico de transações |
| GET | `/api/v1/payments/{id}` | Detalhe de uma transação |
| GET | `/api/v1/observability` | Efeitos acumulados dos observadores (mensageria/métricas) |
| GET | `/docs` | Swagger UI |
| GET | `/api-docs` | Especificação OpenAPI 3 |

### Criar um pagamento

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

Resposta (201):

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

Erros: `400` payload inválido, `404` transação inexistente, `422` veto de risco (com o motivo no campo `detail`).

## Como rodar

Requisitos: Java 21 e Maven.

```bash
./mvnw verify        # build + 26 testes
./mvnw spring-boot:run
# API em http://localhost:9090 — Swagger em /docs
```

## Taxas aplicadas

| Método | Política |
|---|---|
| PIX | fixa R$ 0,49 |
| BOLETO | fixa R$ 2,49 |
| CREDIT_CARD | 3,99% do valor |
| DEBIT_CARD | 1,89% do valor |

## Decisões de design

- **Cadeia por requisição.** A primeira versão tinha a cadeia como bean singleton; a segunda requisição HTTP encontrava o ponteiro no fim e passava por todas as checagens de risco. O bug foi pego por teste de integração (esperava `422`, voltava `201`) e resolvido com um factory que cria cadeia nova a cada pagamento. É o tipo de defeito que passa em review e estoura em produção.
- **`BigDecimal` em dinheiro**, com arredondamento `HALF_EVEN` de 2 casas — nunca `double`.
- **API versionada** (`/api/v1`) e erros no formato RFC 7807 (ProblemDetail).
- **Sem banco de dados.** O `PaymentRegistry` persiste em memória para manter o foco nos padrões; a interface de repositório deixa o caminho pronto para trocar por JPA sem tocar no service.
- **Sem gateway externo.** Os números são uma simulação honesta: as taxas são reais de mercado, mas nenhum pagamento acontece.

## Referências

- [Lab "Explorando Padrões de Projeto na Prática com Java" — DIO](https://github.com/digitalinnovationone/lab-padroes-projeto-java)
- [Refactoring Guru — Design Patterns](https://refactoring.guru/design-patterns)

