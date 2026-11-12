# Roteiro de Apresentação — 5 minutos

> **Objetivo:** apresentar o MVP de forma clara, técnica e dentro do tempo.
> **Estrutura:** 5 blocos cronometrados + demonstração curta.
> **Regra de ouro:** não ler slides; usar os slides como apoio e falar com naturalidade.

---

## Mapa de tempo

| Tempo | Bloco | Slide |
| --- | --- | --- |
| 0:00 – 0:30 | Abertura e objetivo | 1 |
| 0:30 – 1:00 | Problema e solução | 2 e 3 |
| 1:00 – 1:50 | Arquitetura | 4 |
| 1:50 – 2:25 | Tecnologias | 5 |
| 2:25 – 2:45 | Banco de dados | 6 |
| 2:45 – 3:05 | APIs | 7 |
| 3:05 – 4:20 | Demonstração | 8 |
| 4:20 – 4:45 | Resultados | 9 |
| 4:45 – 5:00 | Conclusão e convite a perguntas | 10 |

> Se estourar o tempo, corte a demonstração para **3 chamadas** (emitir → chamar → finalizar) e mantenha os resultados.

---

## Roteiro falado

### 0:00 – 0:30 · Abertura e objetivo (Slide 1)

> “Boa tarde. Meu nome é Carlos Gomes Miranda e vou apresentar o MVP de uma **API de Fila de Atendimento**, desenvolvida para o Programa Residência em TIC.
>
> O objetivo foi construir o backend completo de um sistema de filas: emitir senha, chamar em ordem de chegada, registrar o atendimento e gerar indicadores. A stack é **Java 21, Spring Boot 3, PostgreSQL e Docker**.”

**Dica:** diga o objetivo em uma frase e já avance. Não explique o sumário.

---

### 0:30 – 1:00 · Problema e solução (Slides 2 e 3)

> “O problema é conhecido: senhas de papel se perdem, chamadas saem de ordem, não existe histórico e a gestão não tem métrica.
>
> A solução digitaliza o ciclo completo: o cidadão **emite** uma senha com numeração diária por fila — `A001`, `A002` — **acompanha** a posição, é **chamado** em FIFO e o atendente registra **início e fim**. Toda transição fica **auditada**.
>
> O escopo é API-only: o contrato REST está pronto para totens, apps e painéis.”

**Transição:** “Para sustentar isso, veja como a arquitetura foi organizada.”

---

### 1:00 – 1:50 · Arquitetura (Slide 4)

> “É um monólito modular em camadas, inspirado em Clean Architecture: **Controller → Service → Repository → Entity**.
>
> O controller só trata HTTP e validação. O service concentra regra de negócio e transação. O repository isola o banco com Spring Data e Specifications. As entidades protegem invariantes — por exemplo, uma senha só pode ser chamada se estiver `AGUARDANDO`.
>
> Dois pontos de concorrência foram tratados de propósito: a **emissão** usa lock pessimista na fila para não duplicar números; a **chamada** usa lock pessimista na busca FIFO para dois atendentes não chamarem a mesma senha.
>
> DTOs e MapStruct desacoplam o contrato do banco, e os erros seguem o **RFC 9457**.”

**Dica:** aponte para o diagrama e siga a seta uma única vez.

---

### 1:50 – 2:25 · Tecnologias (Slide 5)

> “No stack: **Java 21** com records e `java.time`; **Spring Boot 3.5** com Web, Data JPA, Security, Validation e Actuator; **PostgreSQL 16** com **Flyway**; segurança com **JWT HS256** e **BCrypt**; documentação com **springdoc/Swagger**; mapeamento com **MapStruct** e **Lombok**.
>
> Em qualidade: **JUnit 5**, Mockito, MockMvc, H2 em modo PostgreSQL e **JaCoCo** com gate de 80%. Em infraestrutura: **Docker multi-stage** e **Compose** com healthchecks.”

**Transição:** “Falando em banco, veja o modelo.”

---

### 2:25 – 2:45 · Banco de dados (Slide 6)

> “São cinco tabelas: `filas`, `usuarios`, `senhas`, `atendimentos` e `senha_eventos`.
>
> A relação central é **fila 1:N senha** e **senha 1:1 atendimento** — o `UNIQUE` em `senha_id` garante a regra de um atendimento por senha. A tabela `senha_eventos` registra cada transição com autor e horário.
>
> O schema é versionado por **Flyway** em três migrações e o Hibernate apenas valida: o banco é a fonte da verdade.”

---

### 2:45 – 3:05 · APIs (Slide 7)

> “São **24 operações em 17 paths**, agrupadas em autenticação, senhas, fila, atendimentos, filas e usuários.
>
> O contrato tem validação com mensagem por campo, paginação, filtros, `201` com `Location` e erros padronizados com `codigo` e `traceId`. Tudo navegável no Swagger.”

**Transição:** “Agora vou mostrar o fluxo funcionando.”

---

### 3:05 – 4:20 · Demonstração (Slide 8)

**Preparação (antes de começar):** stack já no ar e Swagger aberto.

> “Vou executar o fluxo completo em menos de um minuto.”

1. **Terminal**
   ```bash
   docker compose ps
   ```
   > “API e banco estão **healthy**.”

2. **Swagger → `POST /api/auth/login`**
   > “Autentico como atendente e recebo o JWT. Autorizo no Swagger.”

3. **`POST /api/senhas`**
   > “Emito a senha: **201 Created**, número `A001`, posição 1.”

4. **`GET /api/fila`**
   > “O painel mostra a senha em `AGUARDANDO`.”

5. **`POST /api/fila/proxima`** com `{"filaId":1,"guiche":"G1"}`
   > “Chamo a próxima: status `CHAMADA`, atendimento criado e guichê G1.”

6. **`POST /api/atendimentos/iniciar`**
   > “Inicio o atendimento: `EM_ATENDIMENTO`.”

7. **`POST /api/atendimentos/finalizar`**
   > “Finalizo: `ATENDIDA`, com duração registrada.”

8. **`GET /api/fila/estatisticas`**
   > “Estatísticas do dia: total emitido, atendidas e tempos médios.”

9. **Erro proposital (opcional)**
   > “Se eu tentar iniciar uma senha que não foi chamada, recebo **409** com `TRANSICAO_INVALIDA` — a regra está no domínio.”

**Plano B:** se a rede ou o Docker falhar, abra os prints salvos e a coleção Postman; diga: “a demonstração já foi validada e os resultados estão nos slides”.

---

### 4:20 – 4:45 · Resultados (Slide 9)

> “Os resultados: **120 testes**, zero falhas, **93,3% de cobertura de linhas**, gate de 80% atendido, 24 endpoints documentados, três migrações aplicadas e a stack inteira saudável no Docker.
>
> Isso significa que as regras de negócio, o contrato HTTP e a segurança foram verificados — não é apenas ‘funciona na minha máquina’.”

---

### 4:45 – 5:00 · Conclusão (Slide 10)

> “Como principais aprendizados: modelagem de domínio rico, tratamento de concorrência com locks e a importância de medir qualidade.
>
> Como próximos passos: frontend com painel em tempo real, refresh token, rate limiting, Testcontainers e CI/CD.
>
> Obrigado! Fico à disposição para perguntas.”

---

## Checklist pré-apresentação

- [ ] Stack no ar (`docker compose ps` com os dois containers `healthy`)
- [ ] Swagger aberto em `http://localhost:8080/swagger-ui/index.html`
- [ ] Token de login já testado
- [ ] Banco resetado para o seed (`docker compose down -v && docker compose up -d`) — opcional
- [ ] Prints de backup em uma pasta local
- [ ] Slides abertos no modo apresentação
- [ ] Cronômetro visível
- [ ] Água por perto 😄

---

## Perguntas prováveis (respostas de 20 segundos)

| Pergunta | Resposta |
| --- | --- |
| Por que lock pessimista? | Operação curta e crítica; evita duplicidade de número e chamada dupla. O `@Version` cobre o resto. |
| Por que o atendimento nasce na chamada? | Para separar tempo de espera (geração → chamada) do tempo de atendimento (início → fim). |
| Como a numeração reinicia? | `Fila.proximaSequencia` compara a data de referência e volta a 1 na virada do dia. |
| Por que não há frontend? | Escopo aprovado: API-only. O contrato está pronto para qualquer cliente. |
| Como garantem o schema? | Flyway versiona e `ddl-auto=validate` falha se a entidade divergir. |
| O que acontece se o token expirar? | Retorna 401; o cliente precisa autenticar novamente (refresh token é evolução). |
| Por que H2 e não Testcontainers? | Simplicidade e velocidade no MVP; Testcontainers está no roadmap. |
| Qual foi a maior dificuldade? | Garantir consistência na concorrência e manter os horários coerentes entre JVM, banco e container. |

---

## Frases de encerramento (escolha uma)

1. “A fila é um problema simples de explicar e complexo de garantir — e foi essa garantia que o projeto entregou.”
2. “Mais do que endpoints, entreguei um contrato confiável, testado e reproduzível.”
3. “O sistema está pronto para receber uma interface — o backend não é o gargalo.”
