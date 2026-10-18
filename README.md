# kodapay-api

[![Java 21](https://img.shields.io/badge/Java-21-%23007396?style=flat-square)](https://adoptium.net/)   ![status: concluido](https://img.shields.io/badge/status-concluido-green)   ![licenca: MIT](https://img.shields.io/badge/licenca-MIT-blue)

## Resumo

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21 | Payment REST API with 6 GoF patterns
## Problema / Por que existe

O projeto aborda um recorte prático e automatiza uma tarefa que, feita manualmente, é repetitiva e propensa a erro. O foco está em transformar dados ou processos em saída confiável, com código legível, testável e extensível.
## Arquitetura

O código está organizado por responsabilidade em 0 classes principais:
## O que faz

API REST de pagamentos com 6 padrões GoF, Spring Boot 3 e Java 21 | Payment REST API with 6 GoF patterns
## Como rodar

Requisitos: Java 21 e Maven.

```bash
./mvnw verify     # build e executa os testes
./mvnw spring-boot:run
# API em http://localhost:8080
```
Se houver `application.yml`, ajuste a porta antes de rodar.
## Estrutura do Projeto (Classes Principais)


## Trade-offs e Decisões Técnicas

- Cadeia nova por requisição: manter a cadeia como bean singleton fez regras de risco pararem de executar após a primeira requisição. A fábrica agora cria uma cadeia nova a cada pagamento.
- `BigDecimal` para dinheiro, nunca `double`, com `HALF_EVEN` de duas casas.
- API versionada e erros no formato RFC 7807 (ProblemDetail).
- Sem banco de dados: o registro mantém transações em memória para manter o foco nos padrões; a interface de repositório deixa o caminho aberto para trocar por JPA sem mexer no serviço.
- Sem gateway externo: é uma simulação honesta, nenhum pagamento real acontece.
- Mensageria e métricas simuladas em memória, expostas apenas pelo endpoint de observabilidade.
## Limitações Conhecidas

- Histórico em memória: reiniciar a aplicação apaga as transações registradas.
- Não há autenticação nem autorização.
- Mensageria e métricas não integram provedores externos (Kafka, Prometheus, etc.).
- Lista restrita e limites de velocidade fixos no código (não configuráveis).
## Testes

```bash
./mvnw test
```
O repositório contém testes automatizados (JUnit 5, Mockito, MockMvc).
## Licença

Distribuído sob a licença MIT. Consulte o arquivo `LICENSE` do repositório.
## Autor

Kelvin Oliveira ,  https://github.com/KelvinOliveiraCode
Portfolio: https://kelvinoliveiracode.github.io/portfolio/
