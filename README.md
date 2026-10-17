<<<<<<< HEAD
# KodaPay API

![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)
![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-%236DB33F?style=flat-square)
![CI](https://github.com/KelvinOliveiraCode/kodapay-api/actions/workflows/ci.yml/badge.svg)

API REST de processamento de pagamentos com Java 21 e Spring Boot 3 que aplica padrões de projeto GoF (Strategy, Chain of Responsibility, Observer, Factory, Singleton e Facade) no fluxo real de uma transação: taxa, risco, persistência e notificação.

## Problema / Por que existe

Processar um pagamento exige coordenar responsabilidades que não devem viver juntas: a taxa muda conforme o método de pagamento, as regras de risco devem ser avaliadas em ordem fixa, a transação precisa ser registrada com trilha de auditoria e sistemas externos precisam ser avisados — tudo sem travar o fluxo principal. Colocar isso em uma única camada de serviço gera condicionais difíceis de testar e de mudar. Cada responsabilidade aqui fica isolada no padrão que lhe cabe, de modo que incluir um novo método de pagamento ou uma nova regra de risco não exige alterar o código existente.

## O que faz

- Expõe POST /api/v1/payments para processar um pagamento (pagador, descrição, valor, método).
- Calcula a taxa com base no método: Pix R$ 0,49 fixo, boleto R$ 2,49 fixo, crédito 3,99% do valor, débito 1,89% do valor.
- Executa três regras de risco em cadeia: valor acima de R$ 15.000,00, pagador na lista restrita e mais de três transações do mesmo pagador na última hora.
- Recusa a requisição com HTTP 422 quando alguma regra veta, informando o motivo.
- Persiste a transação aprovada com a trilha de auditoria completa.
- Publica um evento de conclusão; listeners de mensageria e métricas reagem sem o fluxo principal conhecê-los, visíveis em GET /api/v1/observability.
- Consulta o histórico e o detalhe de transações por id.
- Serve documentação interativa em GET /docs (Swagger UI) e a especificação OpenAPI 3 em GET /api-docs.

## Stack

- Java 21
- Spring Boot 3.5.16 (Web, Validation)
- Maven
- springdoc-openapi 2.8.17
- JUnit 5, Mockito, MockMvc — 26 testes
- GitHub Actions (JDK 21 Temurin, mvn -B verify)

## Como funciona

Pipeline de pagamento

1. PaymentController valida o payload (bean validation) e delega para PaymentService, que atua como fachada dos subsistemas.
2. PaymentService escolhe a estratégia de taxa pelo método (Strategy). Um novo método de pagamento significa uma nova classe anotada com @Component; nada no service muda.
3. Risco: o factory cria uma cadeia nova por requisição (Factory + Chain of Responsibility). Os três handlers executam na ordem dada por @Order: limite de valor (1), lista restrita (2), velocidade (3). Qualquer veto interrompe o restante.
4. Se aprovado, PaymentRegistry — único registro por JVM (Singleton) — grava a transação.
5. PaymentService publica PaymentEvent (Observer); MessagingListener e MetricsListener reagem de forma desacoplada.
6. Resposta 201 com o pagamento, o total (valor + taxa) e a trilha de auditoria.

Todos os cálculos monetários usam BigDecimal com arredondamento HALF_EVEN de duas casas.

Endpoints

| Método | Rota | Descrição |
|---|---|---|
| POST | /api/v1/payments | Processa um pagamento (retorna taxa, total e trilha de auditoria) |
| GET | /api/v1/payments | Histórico de transações |
| GET | /api/v1/payments/{id} | Detalhe de uma transação |
| GET | /api/v1/observability | Efeitos acumulados dos observadores (mensageria + métricas) |
| GET | /docs | Swagger UI |
| GET | /api-docs | Especificação OpenAPI 3 |

Exemplo de requisição

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

Resposta (201)

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

Os códigos de erro possíveis são 400 (payload inválido), 404 (transação inexistente) e 422 (veto de risco, com o motivo no campo detail, RFC 7807).

## Como rodar

Requisitos: Java 21 e Maven.

```bash
./mvnw verify        # build e executa os 26 testes
./mvnw spring-boot:run
# API em http://localhost:9090 — Swagger em /docs
```

## Estrutura do projeto

```
src/main/java/com/kodapay/
  chain/        RiskHandler (3 elos: valor, lista restrita, velocidade), RiskChain,
                RiskChainFactory (cadeia nova por pagamento), PaymentRegistry (registro único)
  domain/       Payment, PaymentMethod, PaymentStatus, AuditEvent
  dto/          PaymentRequest, PaymentResponse, PaymentView
  exception/    InvalidPaymentException, PaymentNotFoundException, RiskViolationException
  observer/     PaymentEvent, PaymentEventPublisher, MessagingListener, MetricsListener
  service/      PaymentService (fachada: taxa, risco, registro, evento)
  strategy/     FeeCalculator + Pix, Boleto, CreditCard, DebitCard
  web/          PaymentController, ObservabilityController, GlobalExceptionHandler
src/test/       26 testes divididos em cadeia, service, estratégias e controller
pom.xml         Java 21, Spring Boot 3.5.16, springdoc-openapi 2.8.17
src/main/resources/application.yml   porta 9090, caminhos /docs e /api-docs
```

## Trade-offs e decisões

- Cadeia nova por requisição. A primeira versão mantinha a cadeia como bean singleton: a segunda requisição encontrava o ponteiro no fim e nenhuma regra de risco rodava. Um teste de integração pegou o problema (esperava 422, voltou 201). O RiskChainFactory agora cria uma cadeia nova a cada pagamento. É o tipo de defeito que passa em review e estoura em produção.
- BigDecimal para dinheiro, nunca double, com arredondamento HALF_EVEN de duas casas.
- API versionada (/api/v1) e erros no formato RFC 7807 (ProblemDetail).
- Sem banco de dados. PaymentRegistry mantém as transações em memória para manter o foco nos padrões; a interface de repositório deixa o caminho pronto para trocar por JPA sem tocar no service.
- Sem gateway externo. As taxas são baseadas no mercado, mas nenhum pagamento real acontece — é uma simulação honesta.
- Mensageria e métricas são simuladas em memória (listeners ArrayList), expostas apenas pelo endpoint de observabilidade.

## Limitações conhecidas

- Histórico em memória: reiniciar a aplicação apaga as transações registradas.
- Não há autenticação nem autorização.
- Mensageria e métricas não integram provedores externos.
- A lista restrita de risco é um conjunto fixo no código.
- A regra de velocidade conta apenas transações da instância JVM em execução.

## Licença

MIT

## Autor

Kelvin Oliveira — github.com/KelvinOliveiraCode
=======
# KodaPay API — API REST de Pagamentos

![linguagem: Java](https://img.shields.io/badge/linguagem-Java-blue)  
![status: concluido](https://img.shields.io/badge/status-concluido-green)  
![licenca: MIT](https://img.shields.io/badge/licenca-MIT-blue)  
![testes: 26 testes CI verde](https://img.shields.io/badge/testes-26%20testes%20CI%20verde-brightgreen)

### Descrição
API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21.

### O que faz
API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21.

### Tecnologias
- Java 21
- Spring Boot 3
- GoF Patterns
- REST

### Como usar
Execute os testes com: `./mvnw test` (ou `mvn test`).

### Estrutura
Consulte a árvore do repositório para a organização exata de pastas e arquivos.

### Testes
Este repositório inclui uma suíte de testes automatizados: 26 testes CI verde.

### Licença
MIT

### Autor
Kelvin Oliveira — github.com/KelvinOliveiraCode

---

# KodaPay API — Payment REST API

![language: Java](https://img.shields.io/badge/language-Java-blue)  
![status: concluido](https://img.shields.io/badge/status-concluido-green)  
![license: MIT](https://img.shields.io/badge/license-MIT-blue)  
![tests: 26 testes CI verde](https://img.shields.io/badge/tests-26%20testes%20CI%20verde-brightgreen)

### Description
Payment REST API with 6 GoF patterns, Spring Boot 3 and Java 21.

### What it does
Payment REST API with 6 GoF patterns, Spring Boot 3 and Java 21.

### Technologies
- Java 21
- Spring Boot 3
- GoF Patterns
- REST

### How to use
Run tests with: `./mvnw test` (or `mvn test`).

### Structure
Check the repository tree for the exact folder and file organization.

### Tests
This repository includes an automated test suite: 26 testes CI verde.

### License
MIT

### Author
Kelvin Oliveira — github.com/KelvinOliveiraCode
>>>>>>> 8310e5c179faf2a75d0a80c50157e68618deae2a
