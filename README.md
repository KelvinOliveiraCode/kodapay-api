# kodapay-api

<div align="center">

[English](README.en.md) · **Português (Brasil)**

</div>

[![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)](https://adoptium.net/) [![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3-%236DB33F?style=flat-square)](https://spring.io/projects/spring-boot) [![Maven](https://img.shields.io/badge/Maven-3.9-C71A36?style=flat-square&logo=apache-maven)](https://maven.apache.org/) [![JUnit 5](https://img.shields.io/badge/JUnit-5-25A162?style=flat-square&logo=junit5)](https://junit.org/junit5/) [![MIT License](https://img.shields.io/badge/License-MIT-f4c430?style=flat-square)](LICENSE)

### API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21

---

## O que é

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21

---

## Início Rápido

```bash
# Clone e entre
git clone https://github.com/KelvinOliveiraCode/<repo>.git
cd <repo>

# Build e execute os testes
./mvnw verify

# Execute a aplicação
./mvnw spring-boot:run
# API disponível em http://localhost:8080
# Swagger UI em http://localhost:8080/docs
```

## Como funciona

Estrutura limpa e modular com separação de responsabilidades.

---

## Capacidades

| Capacidade | Descrição |
|------------|-------------|
| API REST | Spring Boot 3, endpoints versionados, erros RFC 7807 |
| Cálculo de Taxa | Strategy pattern ,  PIX R$0,49, Boleto R$2,49, Crédito 3,99%, Débito 1,89% |
| Validação de Risco | Chain of Responsibility ,  HighValue -> Blocklist -> Velocity |
| Registro de Transações | Registry Singleton em memória com trilha de auditoria completa |
| Sistema de Eventos | Observer pattern ,  mensageria e métricas desacopladas |
| Observabilidade | Endpoint /api/v1/observability expondo efeitos dos listeners |
| Documentação da API | springdoc-openapi ,  Swagger UI em /docs, OpenAPI 3 em /api-docs |
| Testes | JUnit 5, Mockito, MockMvc ,  testes de integração + unidade |


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

## Perguntas Frequentes

<details>
<summary><strong>Por que uma nova cadeia por requisição?</strong></summary>

Uma cadeia singleton manteria seu ponteiro interno no final após a primeira requisição, fazendo com que todas as checagens de risco subsequentes fossem puladas. A fábrica cria uma cadeia nova a cada vez.

</details>

<details>
<summary><strong>Por que BigDecimal e não double?</strong></summary>

Cálculos financeiros exigem representação decimal exata. double introduz erros de ponto flutuante (ex.: 0,1 + 0,2 ≠ 0,3). BigDecimal com arredondamento HALF_EVEN garante precisão de centavos.

</details>

<details>
<summary><strong>Posso trocar o registro em memória por um banco de dados?</strong></summary>

Sim. O registro está atrás de uma interface; substitua a implementação por JPA/Hibernate sem tocar na camada de serviço.

</details>

<details>
<summary><strong>Está pronto para produção?</strong></summary>

É um **projeto de estudo** demonstrando padrões GoF. Falta auth, mensageria externa e persistência ,  adicione esses para produção.

</details>



---

## Documentação

- **Código-Fonte** ,  Navegue pelo diretório `src/` para detalhes de implementação
- **Referência da API** ,  Swagger UI em `/docs` ao executar
- **Diagrama de Arquitetura** ,  Veja [architecture.mermaid](docs/architecture.mermaid) (se presente)
- **Changelog** ,  [CHANGELOG.md](CHANGELOG.md) (se presente)
- **Contribuindo** ,  [CONTRIBUTING.md](CONTRIBUTING.md) (se presente)

## Licença

[MIT](LICENSE) ,  Kelvin Oliveira

---

<div align="center">

**Construído com** · Java 21 + Spring Boot 3 + Maven

**Autor** · [Kelvin Oliveira](https://github.com/KelvinOliveiraCode) · [Portfolio](https://kelvinoliveiracode.github.io/portfolio/)

</div>