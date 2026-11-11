# Checklist de Entrega ao Professor

> Projeto: **API de Fila de Atendimento** · Versão **1.0.0**
> Use este checklist antes de enviar o link do repositório e o material da apresentação.

---

## 1. Repositório e versionamento

- [ ] Repositório criado no GitHub (público ou com acesso ao professor)
- [ ] `README.md` na raiz, renderizando corretamente no GitHub
- [ ] `.gitignore` funcionando (sem `target/`, `.env`, `*.log`, IDE)
- [ ] `.env` **não** versionado; `.env.example` presente
- [ ] Histórico de commits com mensagens claras (evitar um único commit gigante)
- [ ] Tag de versão criada (ex.: `v1.0.0`)
- [ ] Nenhum segredo real no código (JWT secret, senhas de produção)
- [ ] Licença definida (MIT ou conforme o curso)

## 2. Código-fonte

- [ ] `src/main/java` completo e compilando
- [ ] `src/test/java` completo e passando
- [ ] Pacotes organizados: `config`, `controller`, `dto`, `entity`, `exception`, `mapper`, `repository`, `security`, `service`, `util`
- [ ] Sem código comentado ou arquivos temporários
- [ ] `pom.xml` com versões fixadas e plugins (MapStruct, JaCoCo, Spring Boot)
- [ ] Maven Wrapper (`mvnw`, `mvnw.cmd`, `.mvn/`) presente
- [ ] Código sem `System.out.println` (uso de logs)
- [ ] Sem credenciais hardcoded em classes

## 3. Build e testes

- [ ] `./mvnw clean verify` termina com **BUILD SUCCESS**
- [ ] **120 testes**, 0 falhas, 0 erros
- [ ] Cobertura de linhas ≥ 80% (**93,3%** no relatório completo)
- [ ] Relatório JaCoCo disponível em `target/site/jacoco/index.html`
- [ ] Gate de cobertura configurado no `pom.xml`
- [ ] Nenhum teste ignorado (`@Disabled`) sem justificativa

## 4. Banco de dados e migrações

- [ ] `V1__criar_tabelas.sql` aplicada
- [ ] `V2__criar_indices.sql` aplicada
- [ ] `V3__dados_iniciais.sql` aplicada
- [ ] `flyway_schema_history` com as 3 migrações `success = t`
- [ ] Seed com 2 filas e 3 usuários
- [ ] `ddl-auto=validate` configurado
- [ ] `open-in-view=false`
- [ ] Fuso horário consistente (`APP_TIMEZONE`/`TZ`)
- [ ] Banco limpo para entrega (apenas seed) — opcional recomendado:
      `docker compose down -v && docker compose up -d`

## 5. Docker

- [ ] `Dockerfile` multi-stage funcionando
- [ ] Imagem executando como usuário não-root
- [ ] `docker-compose.yml` com PostgreSQL + API
- [ ] Healthchecks de banco e API
- [ ] Volume `fila_pgdata` configurado
- [ ] `.env.example` com todas as variáveis
- [ ] `docker compose up --build -d` sobe sem erro
- [ ] `docker compose ps` mostra **fila-api (healthy)** e **fila-postgres (healthy)**
- [ ] `docker compose down -v` limpa corretamente

## 6. API e contrato

- [ ] Swagger UI acessível em `/swagger-ui/index.html`
- [ ] OpenAPI JSON em `/v3/api-docs` (17 paths)
- [ ] Título “API - Fila de Atendimento” e versão 1.0.0
- [ ] Esquema `bearerAuth` configurado
- [ ] Todos os endpoints com `summary` e `tag`
- [ ] Erros no padrão RFC 9457
- [ ] Paginação e filtros documentados
- [ ] `POST` de criação retornando `201` + `Location`

## 7. Segurança

- [ ] Sem token → **401**
- [ ] Perfil `USUARIO` em `/api/usuarios` → **403**
- [ ] Perfil `USUARIO` em `/api/senhas` → **200**
- [ ] Senhas armazenadas com BCrypt
- [ ] Token com expiração configurável
- [ ] Sessão stateless e CSRF desabilitado
- [ ] CORS configurável por ambiente
- [ ] Stack trace não exposto nas respostas

## 8. Validação funcional (smoke test)

- [ ] Login com `atendente@fila.local` retorna token
- [ ] Emissão de senha retorna `A001` (banco limpo)
- [ ] Segunda emissão retorna `A002`
- [ ] Painel lista a senha em `AGUARDANDO`
- [ ] Chamada retorna `CHAMADA` + `atendimentoId`
- [ ] Início retorna `EM_ATENDIMENTO`
- [ ] Finalização retorna `ATENDIDA` + duração
- [ ] Estatísticas retornam totais e tempos médios
- [ ] Histórico paginado retorna o atendimento
- [ ] Tentativa de transição inválida retorna **409**

## 9. Documentação

- [ ] `README.md` — descrição, funcionalidades, tecnologias, instalação, configuração, execução, Swagger, Docker, estrutura e prints sugeridos
- [ ] `docs/REQUISITOS.md` — RF, RNF, RN e rastreabilidade
- [ ] `docs/RELATORIO-ACADEMICO.md` — relatório completo
- [ ] `docs/ARQUITETURA.md` — camadas, diagramas e decisões
- [ ] `docs/MER.md` — DER e dicionário de dados
- [ ] `docs/API.md` — endpoints, payloads e erros
- [ ] `docs/TESTES.md` — estratégia, suítes e cobertura
- [ ] `docs/APRESENTACAO.md` — 10 slides
- [ ] `docs/ROTEIRO-APRESENTACAO.md` — roteiro de 5 minutos
- [ ] `docs/RESUMO-EXECUTIVO.md` — uma página
- [ ] `docs/CHECKLIST-ENTREGA.md` — este checklist
- [ ] Coleção Postman (`docs/FilaAtendimento.postman_collection.json`)

## 10. Apresentação

- [ ] Slides revisados e sem placeholders (`_(seu nome)_`)
- [ ] Diagramas Mermaid renderizando (VS Code/Obsidian/Marp)
- [ ] Roteiro ensaiado dentro de **5 minutos**
- [ ] Demonstração testada com a stack no ar
- [ ] Prints de backup salvos (caso a rede falhe)
- [ ] Perguntas prováveis revisadas
- [ ] Áudio/vídeo testado (se apresentação remota)
- [ ] Link do GitHub e do Swagger à mão

## 11. Evidências para o relatório

- [ ] Print de `docker compose ps` com os dois containers healthy
- [ ] Print do Swagger UI com os 17 paths
- [ ] Print de emissão de senha (`201`, `A001`)
- [ ] Print do painel da fila
- [ ] Print de chamada/início/finalização
- [ ] Print das estatísticas
- [ ] Print de erro 401/403/409
- [ ] Print do `mvn verify` com 120 testes e BUILD SUCCESS
- [ ] Print do relatório JaCoCo (93,3%)
- [ ] Print das tabelas e do `flyway_schema_history`
- [ ] Print da estrutura de pacotes na IDE

## 12. Submissão final

- [ ] Link do repositório enviado no formato solicitado
- [ ] `docs/RESUMO-EXECUTIVO.md` anexado ou convertido em PDF
- [ ] Relatório acadêmico em PDF (se exigido)
- [ ] Slides em PDF (se exigido)
- [ ] Nome, matrícula e turma preenchidos
- [ ] Prazo conferido no calendário do curso
- [ ] Cópia de segurança do projeto (zip ou branch `entrega`)
- [ ] Mensagem de envio revisada (tom profissional e objetivo)

---

## Comandos de validação final

```bash
# 1. Build + testes + cobertura
./mvnw clean verify

# 2. Stack completa
docker compose down -v
docker compose up --build -d
docker compose ps

# 3. Health
curl -s http://localhost:8080/actuator/health

# 4. OpenAPI
curl -s http://localhost:8080/v3/api-docs | head -c 300

# 5. Login
curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"atendente@fila.local","senha":"Atendente@123"}'
```

> **Critério de pronto:** todos os itens acima marcados, `BUILD SUCCESS`, containers `healthy`, Swagger acessível e demonstração ensaiada.

---

**Boa apresentação! 🚀**
