# kodapay-api

![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)   ![status: concluido](https://img.shields.io/badge/status-concluido-green)   ![licenca: MIT](https://img.shields.io/badge/licenca-MIT-blue)

## Resumo

API REST em Java 21 com Spring Boot 3.5.16, desenvolvida como estudo de arquitetura de software. API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21

## Problema / por que existe

Em aplicacoes pequenas, regras de negocio, validacao, persistencia e notificacao costumam ficar misturadas em uma unica camada de servico. Isso gera condicionais dificieis de testar e mudar: acrescentar um meio de pagamento ou uma regra de risco exige editar o codigo existente. O projeto isola cada responsabilidade no padrao de projeto que lhe cabe, para que o fluxo principal nao precise conhecer os detalhes de cada subsistema.

## Arquitetura

O fluxo segue um pipeline em camadas:

1. Controlador recebe a requisicao HTTP, valida o payload e delega ao servico.
2. Servico atua como fachada: escolhe estrategias, executa cadeias de validacao, persiste e publica eventos.
3. Estrategias isolam o calculo variavel (ex: taxa por meio de pagamento).
4. Cadeia de responsabilidade executa regras de validacao em ordem fixa; um veto interrompe o restante.
5. Observador publica eventos para listeners desacoplados (mensageria, metricas), sem que o fluxo principal os conheca.
6. Registro unico mantem as transacoes em memoria na JVM.

Operacoes monetarias usam `BigDecimal` com `HALF_EVEN` de duas casas, nunca `double`.

## Tecnologias

- Java 21
- Spring Boot 3.5.16 (Web, Validation)
- Maven
- springdoc-openapi (Swagger UI / OpenAPI 3)
- JUnit 5, Mockito, MockMvc
- GitHub Actions (CI)

Dependencias diretas do `pom.xml`:
- kodapay-api
- spring-boot-starter-web
- spring-boot-starter-validation
- spring-boot-starter-test
- springdoc-openapi-starter-webmvc-ui


## O que faz

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21

## Como rodar

Requisitos: Java 21 e Maven.

```
./mvnw verify     # build e executa os testes
./mvnw spring-boot:run
# API em http://localhost:8080
```

Se houver `application.yml`, ajuste a porta antes de rodar.

## Estrutura do projeto

- .github/ (1 arquivo(s))
- .mvn/ (1 arquivo(s))
- src/ (1 arquivo(s))
- .gitignore, LICENSE, mvnw, mvnw.cmd, pom.xml, README.en.md, README.md


## Trade-offs e decisões

- Cadeia nova por requisicao: manter a cadeia como bean singleton fez regras de risco pararem de executar apos a primeira requisicao. A fabrica agora cria uma cadeia nova a cada pagamento.
- `BigDecimal` para dinheiro, nunca `double`, com `HALF_EVEN` de duas casas.
- API versionada e erros no formato RFC 7807 (ProblemDetail).
- Sem banco de dados: o registro mantem transacoes em memoria para manter o foco nos padroes; a interface de repositorio deixa o caminho aberto para trocar por JPA sem mexer no servico.
- Sem gateway externo: e uma simulacao honesta, nenhum pagamento real acontece.
- Mensageria e metricas simuladas em memoria, expostas apenas pelo endpoint de observabilidade.

## Limitações conhecidas

- Historico em memoria: reiniciar a aplicacao apaga as transacoes.
- Nao ha autenticacao nem autorizacao.
- Mensageria e metricas nao integram provedores externos.
- Listas e regras fixas no codigo (ex: lista restrita, limite de velocidade).

## Testes

```
./mvnw test
```

O repositorio contem testes automatizados.

## Licença

Distribuído sob a licenca MIT. Consulte o arquivo `LICENSE` do repositorio.

## Autor

Kelvin Oliveira, https://github.com/KelvinOliveiraCode
Portfolio: https://kelvinoliveiracode.github.io/portfolio/
