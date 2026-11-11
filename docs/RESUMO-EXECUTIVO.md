# Resumo Executivo — API de Fila de Atendimento

**Projeto:** Sistema de Fila de Atendimento (backend)
**Curso:** Programa Residência em TIC — Capacita Brasil / iRede
**Autor:** _(seu nome)_ · **Orientador:** _(nome)_
**Repositório:** `https://github.com/<usuario>/<repo>`
**Data:** 11/11/2026

---

## O problema

Filas de atendimento presenciais controladas manualmente geram senhas duplicadas, chamadas fora de ordem, perda de histórico e ausência de indicadores. A instituição não consegue medir espera, produtividade nem responsabilizar operações.

## A solução

Uma **API REST** que digitaliza o ciclo completo do atendimento:

1. **Emissão** de senhas com numeração diária por fila (`A001`, `A002`, …).
2. **Painel e posição** em tempo real para o cidadão.
3. **Chamada FIFO** com controle de concorrência.
4. **Atendimento** com guichê, atendente, início, fim e duração.
5. **Histórico e estatísticas** do dia.
6. **Auditoria** de todas as transições de status.
7. **Segurança** com JWT e três perfis de acesso.

O escopo é **API-only**: o contrato OpenAPI está pronto para alimentar totens, aplicativos e painéis, sem retrabalho de backend.

## Diferenciais técnicos

| Diferencial | Implementação |
| --- | --- |
| Consistência sob concorrência | Lock pessimista na emissão e na chamada FIFO + `@Version` + constraints |
| Domínio rico | Entidades validam transições (Tell, Don't Ask) |
| Contrato profissional | DTOs, MapStruct, Bean Validation, paginação e OpenAPI |
| Erros padronizados | RFC 9457 (`ProblemDetail`) com `codigo` e `traceId` |
| Qualidade medida | 120 testes e 93,3% de cobertura de linhas com gate de build |
| Reprodutibilidade | Docker multi-stage + Compose com healthchecks e Flyway |
| Auditoria | Tabela `senha_eventos` com status, autor, horário e detalhe |

## Stack

**Java 21 · Spring Boot 3.5.16 · Spring MVC · Spring Data JPA · Spring Security · JWT (HS256) · PostgreSQL 16 · Flyway · MapStruct · Lombok · springdoc/Swagger · JUnit 5 · Mockito · JaCoCo · Docker**

## Resultados

| Indicador | Resultado |
| --- | --- |
| Testes automatizados | **120** (82 unitários + 38 de integração) |
| Falhas | **0** |
| Cobertura de linhas | **93,3%** (725/777) |
| Gate JaCoCo (80%) | **Atendido** |
| Endpoints documentados | **24** operações em **17 paths** |
| Migrações Flyway | **3** aplicadas com sucesso |
| Stack Docker | **2 containers healthy** (`fila-api`, `fila-postgres`) |
| Smoke test end-to-end | **OK** (emitir → chamar → iniciar → finalizar → estatísticas) |
| Validação de segurança | 401 sem token · 403 para perfil sem permissão |

## Banco de dados

Cinco tabelas de negócio — `filas`, `usuarios`, `senhas`, `atendimentos` e `senha_eventos` — com chaves estrangeiras, constraints de unicidade, checks de status e sete índices para as consultas críticas. O schema é versionado por Flyway e validado pelo Hibernate (`ddl-auto=validate`).

## Regras de negócio garantidas

- Numeração sequencial por fila, reiniciada diariamente.
- Chamada estritamente FIFO.
- Apenas senhas `AGUARDANDO` podem ser chamadas.
- Um atendimento por senha; finalização exige início.
- Cancelamento definitivo.
- Auditoria obrigatória de toda transição.
- Fila inativa não emite nem chama senhas.
- Exclusões lógicas preservam o histórico.

## Como executar

```bash
docker compose up --build -d
# Swagger: http://localhost:8080/swagger-ui/index.html
```

Credenciais de demonstração:
`admin@fila.local / Admin@123` · `atendente@fila.local / Atendente@123` · `usuario@fila.local / Usuario@123`

## Documentação

- [`README.md`](../README.md) — instalação, execução e funcionalidades
- [`docs/RELATORIO-ACADEMICO.md`](RELATORIO-ACADEMICO.md) — relatório completo
- [`docs/REQUISITOS.md`](REQUISITOS.md) — RF, RNF, RN e rastreabilidade
- [`docs/ARQUITETURA.md`](ARQUITETURA.md) — decisões e diagramas
- [`docs/MER.md`](MER.md) — DER e dicionário de dados
- [`docs/API.md`](API.md) — referência dos endpoints
- [`docs/TESTES.md`](TESTES.md) — estratégia e cobertura

## Conclusão

O projeto entrega um backend consistente, seguro, testado e reproduzível para gestão de filas. Mais do que cumprir requisitos, demonstra maturidade em modelagem de domínio, concorrência, qualidade de código e documentação — competências centrais para o desenvolvimento Java profissional.

**Contato:** _(seu e-mail)_ · **LinkedIn:** _(opcional)_
