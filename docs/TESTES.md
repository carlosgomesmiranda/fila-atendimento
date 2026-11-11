# Testes e Cobertura

Este documento descreve a estratégia de testes, o que cada suíte cobre e como executar/verificar a cobertura.

---

## 1. Resumo

| Métrica | Valor |
| --- | --- |
| Total de testes | **120** |
| Unitários | 82 |
| Integração | 38 |
| Falhas | 0 |
| Cobertura mínima exigida | **80% de linhas** (JaCoCo) |
| Cobertura obtida (escopo do check) | **92,9% de linhas** |
| Cobertura obtida (relatório completo) | **93,3% de linhas** |

O build falha automaticamente (`mvn verify`) se a cobertura de linhas do bundle ficar abaixo de 80%.

---

## 2. Pirâmide adotada

```
                 ┌─────────────────────────┐
                 │   Integração (38)       │  MockMvc + H2 + Flyway
                 │   Controllers, segurança│
                 │   fluxo ponta a ponta   │
                 └───────────┬─────────────┘
                             │
                 ┌───────────▼─────────────┐
                 │   Unitários (82)        │  JUnit 5 + Mockito
                 │   Services, entidades,  │
                 │   mappers, JWT, utils   │
                 └─────────────────────────┘
```

- **Unitários:** isolam a regra de negócio com mocks; são rápidos e cobrem caminhos felizes e de exceção.
- **Integração:** sobem o contexto Spring completo, aplicam Flyway no H2 e exercitam o contrato HTTP real (status, headers, JSON, autenticação e autorização).

---

## 3. Suítes de teste

### 3.1 Unitários

| Classe | Testes | Cobre |
| --- | ---: | --- |
| `entity.SenhaTest` | 11 | Transições de status, cancelamento, estados terminais, flags |
| `entity.FilaTest` | 3 | Numeração diária, reset de sequência, incremento no mesmo dia |
| `service.SenhaServiceTest` | 11 | Emissão, fila inativa/inexistente, última senha, posição, cancelamento, listagem |
| `service.AtendimentoServiceTest` | 10 | Início/fim, atendimento ausente, transições inválidas, listagem |
| `service.FilaServiceTest` | 7 | Painel, chamada FIFO, fila vazia/inativa, estatísticas |
| `service.FilaAdminServiceTest` | 9 | CRUD de filas, duplicidades, normalização de prefixo, desativação |
| `service.UsuarioServiceTest` | 10 | CRUD, e-mail duplicado, hash de senha, autodesativação |
| `service.AuthServiceTest` | 4 | Login, token, `/me`, usuário inexistente |
| `mapper.MapperTest` | 6 | Conversões MapStruct e campos calculados |
| `security.JwtServiceTest` | 5 | Geração, validação, expiração, claims, segredo inválido |
| `security.UserDetailsServiceImplTest` | 3 | Carregamento por e-mail, usuário inativo, authorities |
| `util.NumeroSenhaUtilTest` | 3 | Formatação `A001`, sequências grandes, prefixo |

### 3.2 Integração

Todas estendem `support.AbstractIntegrationTest`, que fornece `MockMvc`, `ObjectMapper`, helpers de login e headers autenticados. A classe é anotada com `@SpringBootTest` + `@AutoConfigureMockMvc` + `@Transactional`, garantindo rollback ao fim de cada teste.

| Classe | Testes | Cobre |
| --- | ---: | --- |
| `ContextIntegrationTest` | 1 | Carregamento do contexto e migrações |
| `AuthControllerIntegrationTest` | 8 | Login válido/inválido, validação, `/me`, 401 |
| `SenhaControllerIntegrationTest` | 11 | Emissão, numeração, filtros, última, posição, cancelamento, autorização |
| `FilaControllerIntegrationTest` | 4 | Fluxo completo emitir → chamar → iniciar → finalizar e estatísticas |
| `FilaAdminControllerIntegrationTest` | 5 | CRUD de filas e restrição a ADMINISTRADOR |
| `UsuarioControllerIntegrationTest` | 7 | CRUD de usuários, duplicidade, perfis e 403 |
| `OpenApiIntegrationTest` | 2 | `/v3/api-docs` e Swagger UI acessíveis |

---

## 4. Como executar

```bash
# apenas os testes
./mvnw test

# testes + relatório + verificação de cobertura
./mvnw verify
```

No Windows (PowerShell), use `.\mvnw.cmd`.

Relatórios gerados:

| Artefato | Caminho |
| --- | --- |
| Relatório HTML | `target/site/jacoco/index.html` |
| CSV | `target/site/jacoco/jacoco.csv` |
| XML | `target/site/jacoco/jacoco.xml` |
| Execução bruta | `target/jacoco.exec` |
| Resultados Surefire | `target/surefire-reports/` |

---

## 5. Configuração dos testes de integração

`src/test/resources/application-test.yml`:

- **H2 em memória** com `MODE=PostgreSQL`, `DATABASE_TO_LOWER=TRUE`, `DEFAULT_NULL_ORDERING=HIGH`;
- `ddl-auto=validate` — o schema é criado pelas **mesmas migrações Flyway** de produção;
- segredo JWT exclusivo de teste;
- logs reduzidos para manter a saída limpa;
- `DB_CLOSE_DELAY=-1` para manter o banco durante o contexto.

Essa combinação garante que o SQL e as constraints validadas em teste sejam os mesmos do PostgreSQL (modo de compatibilidade), reduzindo divergências.

### Isolamento

- Cada teste de integração roda em transação com rollback.
- A sequência diária reinicia por teste (o estado é revertido), o que torna os testes determinísticos.
- Autenticação é feita via login real e uso do token nos headers.

---

## 6. Cobertura e exclusões

O JaCoCo está configurado no `pom.xml` com:

```xml
<limit>
  <counter>LINE</counter>
  <value>COVEREDRATIO</value>
  <minimum>0.80</minimum>
</limit>
```

Exclusões justificadas:

| Exclusão | Motivo |
| --- | --- |
| `FilaAtendimentoApplication` | Apenas `main()` |
| `config/**` | Configuração de infraestrutura (OpenAPI, CORS, Clock, filtros) |
| `dto/**` | Records sem lógica de negócio |
| `mapper/*Impl.class` | Código gerado pelo MapStruct |

As demais classes (controllers, services, entities, security, utils, repositories) entram no cálculo.

---

## 7. Boas práticas aplicadas

- **Arrange/Act/Assert** implícito e nomes descritivos em português (`deve...`).
- Uso de `Clock.fixed` para eliminar dependência de data/hora real.
- Uso de `ArgumentCaptor` para validar o que foi persistido.
- Asserções com **AssertJ** (`assertThat`, `assertThatThrownBy`).
- Testes de integração verificam status HTTP, corpo e headers relevantes.
- Testes de autorização cobrem 401/403 para cada perfil.
- Nenhum teste depende de ordem de execução.

---

## 8. Solução de problemas

| Sintoma | Causa provável | Ação |
| --- | --- | --- |
| `Schema validation: missing table` | Migração não aplicada | Verifique `spring.flyway.enabled` e os scripts em `db/migration` |
| `Port 8080 already in use` | Outra instância rodando | Encerre o processo ou use `SERVER_PORT` |
| Falha de cobertura | Novas classes sem teste | Adicione testes ou revise as exclusões com critério |
| `401` em testes | Token ausente/expirado | Use os helpers de `AbstractIntegrationTest` |
| Teste de posição instável | Ordenação por timestamp | A posição usa `id`, não timestamp; verifique se a query foi alterada |
