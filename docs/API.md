# API — Referência

Base URL local: `http://localhost:8080`

- Swagger UI: `/swagger-ui.html`
- OpenAPI JSON: `/v3/api-docs`
- Autenticação: `Authorization: Bearer <token>`
- Content-Type: `application/json`
- Datas: `YYYY-MM-DD` · Data/hora: ISO-8601 (`2026-11-10T09:00:00`)

---

## 1. Autenticação

### `POST /api/auth/login` — público

Autentica o usuário e devolve o token JWT.

**Request**

```json
{
  "email": "atendente@fila.local",
  "senha": "Atendente@123"
}
```

**Response `200`**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "tipo": "Bearer",
  "expiraEmSegundos": 7200,
  "usuario": {
    "id": 2,
    "nome": "Atendente Padrao",
    "email": "atendente@fila.local",
    "perfil": "ATENDENTE",
    "ativo": true,
    "criadoEm": "2026-11-10T09:00:00"
  }
}
```

**Erros:** `400` (validação), `401` (credenciais inválidas ou usuário inativo).

---

### `GET /api/auth/me` — autenticado

Retorna o usuário do token.

**Response `200`**

```json
{
  "id": 2,
  "nome": "Atendente Padrao",
  "email": "atendente@fila.local",
  "perfil": "ATENDENTE",
  "ativo": true,
  "criadoEm": "2026-11-10T09:00:00"
}
```

---

## 2. Senhas

### `POST /api/senhas` — autenticado

Gera uma nova senha. O corpo é opcional; sem `filaId`, usa a fila de prefixo padrão (`A`).

**Request**

```json
{ "filaId": 1 }
```

**Response `201`** com header `Location: /api/senhas/{id}`

```json
{
  "id": 10,
  "numero": "A001",
  "status": "AGUARDANDO",
  "fila": { "id": 1, "nome": "Geral", "prefixo": "A" },
  "dataGeracao": "2026-11-10T09:00:00",
  "posicao": 1,
  "pessoasNaFrente": 0
}
```

**Erros:** `400` (`filaId` inválido), `404` (fila inexistente), `409` (`FILA_INATIVA`).

---

### `GET /api/senhas` — autenticado

Lista senhas com filtros e paginação.

| Query param | Tipo | Obrigatório | Padrão |
| --- | --- | --- | --- |
| `status` | `AGUARDANDO` \| `CHAMADA` \| `EM_ATENDIMENTO` \| `ATENDIDA` \| `CANCELADA` | não | — |
| `filaId` | `long` | não | — |
| `data` | `YYYY-MM-DD` | não | — |
| `page` | `int` | não | `0` |
| `size` | `int` | não | `20` (máx. 100) |
| `sort` | `campo,direção` | não | `dataGeracao,desc` |

**Exemplo**

```
GET /api/senhas?status=AGUARDANDO&filaId=1&data=2026-11-10&page=0&size=20
```

**Response `200`**

```json
{
  "conteudo": [
    {
      "id": 10,
      "numero": "A001",
      "status": "AGUARDANDO",
      "fila": { "id": 1, "nome": "Geral", "prefixo": "A" },
      "dataGeracao": "2026-11-10T09:00:00",
      "posicao": 1,
      "pessoasNaFrente": 0
    }
  ],
  "pagina": 0,
  "tamanho": 20,
  "totalElementos": 1,
  "totalPaginas": 1,
  "primeira": true,
  "ultima": true
}
```

---

### `GET /api/senhas/ultima` — autenticado

Retorna a última senha emitida **hoje** na fila.

| Query param | Tipo | Obrigatório |
| --- | --- | --- |
| `filaId` | `long` | não (usa fila padrão) |

**Response `200`** — mesmo contrato de `SenhaResponse`.

**Erros:** `404` (`SENHA_NAO_ENCONTRADA` quando não há senha no dia).

---

### `GET /api/senhas/{id}` — autenticado

Consulta uma senha e, se estiver `AGUARDANDO`, sua posição atual.

**Response `200`**

```json
{
  "id": 10,
  "numero": "A001",
  "status": "AGUARDANDO",
  "fila": { "id": 1, "nome": "Geral", "prefixo": "A" },
  "dataGeracao": "2026-11-10T09:00:00",
  "posicao": 3,
  "pessoasNaFrente": 2
}
```

Para status não aguardando, `posicao` e `pessoasNaFrente` são omitidos (serialização `non_null`).

**Erros:** `404` (`SENHA_NAO_ENCONTRADA`).

---

### `POST /api/senhas/{id}/cancelar` — ATENDENTE / ADMINISTRADOR

Cancela definitivamente a senha (RN006). Não retorna à fila.

**Response `200`**

```json
{
  "id": 10,
  "numero": "A001",
  "status": "CANCELADA",
  "fila": { "id": 1, "nome": "Geral", "prefixo": "A" },
  "dataGeracao": "2026-11-10T09:00:00"
}
```

**Erros:** `404`, `409` (`TRANSICAO_INVALIDA` se já terminal).

---

## 3. Fila

### `GET /api/fila` — autenticado

Painel em tempo real: aguardando, chamadas e em atendimento.

| Query param | Tipo | Obrigatório |
| --- | --- | --- |
| `filaId` | `long` | não |

**Response `200`**

```json
{
  "fila": { "id": 1, "nome": "Geral", "prefixo": "A" },
  "aguardando": [
    { "id": 10, "numero": "A001", "status": "AGUARDANDO", "dataGeracao": "2026-11-10T09:00:00" }
  ],
  "chamadas": [],
  "emAtendimento": [],
  "totalAguardando": 1,
  "atualizadoEm": "2026-11-10T09:05:00"
}
```

---

### `GET /api/fila/estatisticas` — autenticado

Estatísticas do dia para a fila.

**Response `200`**

```json
{
  "data": "2026-11-10",
  "totalEmitidas": 12,
  "aguardando": 2,
  "chamadas": 1,
  "emAtendimento": 1,
  "atendidas": 7,
  "canceladas": 1,
  "tempoMedioEsperaSegundos": 184.5,
  "tempoMedioAtendimentoSegundos": 320.0,
  "ultimaSenhaEmitida": "A012",
  "ultimaSenhaChamada": "A010"
}
```

---

### `POST /api/fila/proxima` — ATENDENTE / ADMINISTRADOR

Chama a próxima senha FIFO e cria o atendimento correspondente. Corpo opcional.

**Request**

```json
{ "filaId": 1, "guiche": "G1" }
```

**Response `200`**

```json
{
  "senhaId": 10,
  "numero": "A001",
  "status": "CHAMADA",
  "horaChamada": "2026-11-10T09:05:00",
  "atendimentoId": 5,
  "guiche": "G1",
  "aguardandoRestantes": 0
}
```

**Erros:** `404` (fila), `409` (`FILA_INATIVA`), `409` (`FILA_VAZIA`).

---

## 4. Atendimentos

> Todo o recurso exige **ATENDENTE** ou **ADMINISTRADOR**.

### `POST /api/atendimentos/iniciar`

Inicia o atendimento de uma senha já chamada.

**Request**

```json
{ "senhaId": 10, "guiche": "G1" }
```

**Response `200`**

```json
{
  "id": 5,
  "senhaId": 10,
  "numeroSenha": "A001",
  "statusSenha": "EM_ATENDIMENTO",
  "inicio": "2026-11-10T09:06:00",
  "guiche": "G1",
  "atendenteId": 2,
  "atendenteNome": "Atendente Padrao",
  "duracaoSegundos": null
}
```

**Erros:** `404` (`SENHA_NAO_ENCONTRADA`), `409` (`ATENDIMENTO_NAO_ENCONTRADO` se a senha não foi chamada, `TRANSICAO_INVALIDA` se o status não for `CHAMADA`).

---

### `POST /api/atendimentos/finalizar`

Finaliza um atendimento em andamento.

**Request**

```json
{ "atendimentoId": 5, "observacao": "Atendimento concluído" }
```

**Response `200`**

```json
{
  "id": 5,
  "senhaId": 10,
  "numeroSenha": "A001",
  "statusSenha": "ATENDIDA",
  "inicio": "2026-11-10T09:06:00",
  "fim": "2026-11-10T09:11:20",
  "guiche": "G1",
  "observacao": "Atendimento concluído",
  "atendenteId": 2,
  "atendenteNome": "Atendente Padrao",
  "duracaoSegundos": 320
}
```

**Erros:** `404`, `409` (`TRANSICAO_INVALIDA` se não iniciado ou já finalizado).

---

### `GET /api/atendimentos`

Histórico paginado.

| Query param | Tipo | Obrigatório | Padrão |
| --- | --- | --- | --- |
| `dataInicio` | `YYYY-MM-DD` | não | — |
| `dataFim` | `YYYY-MM-DD` | não | — |
| `guiche` | `string` | não | — |
| `atendenteId` | `long` | não | — |
| `status` | status da senha | não | — |
| `page` | `int` | não | `0` |
| `size` | `int` | não | `20` (máx. 100) |
| `sort` | `campo,direção` | não | `inicio,desc` |

**Response `200`** — `PageResponse<AtendimentoResponse>`.

---

### `GET /api/atendimentos/{id}`

**Response `200`** — `AtendimentoResponse`.

**Erros:** `404` (`ATENDIMENTO_NAO_ENCONTRADO`).

---

## 5. Filas (administração)

### `GET /api/filas` — autenticado

**Response `200`**

```json
[
  {
    "id": 1,
    "nome": "Geral",
    "prefixo": "A",
    "descricao": "Fila de atendimento geral",
    "ativa": true,
    "criadoEm": "2026-11-10T09:00:00",
    "atualizadoEm": "2026-11-10T09:00:00"
  }
]
```

### `GET /api/filas/{id}` — autenticado

**Response `200`** — `FilaDetalheResponse`.

### `POST /api/filas` — ADMINISTRADOR

**Request**

```json
{
  "nome": "Financeiro",
  "prefixo": "F",
  "descricao": "Atendimento financeiro",
  "ativa": true
}
```

**Response `201`** com `Location: /api/filas/{id}`.

**Erros:** `400`, `409` (`FILA_NOME_DUPLICADO`, `FILA_PREFIXO_DUPLICADO`).

### `PUT /api/filas/{id}` — ADMINISTRADOR

Mesmo payload de criação. Atualiza nome, prefixo, descrição e status.

**Response `200`**.

### `DELETE /api/filas/{id}` — ADMINISTRADOR

Desativa a fila (exclusão lógica). **Response `204`**.

---

## 6. Usuários (somente ADMINISTRADOR)

### `GET /api/usuarios`

| Query param | Padrão |
| --- | --- |
| `page` | `0` |
| `size` | `20` (máx. 100) |
| `sort` | `nome,asc` |

**Response `200`** — `PageResponse<UsuarioResponse>`.

### `GET /api/usuarios/{id}`

**Response `200`** — `UsuarioResponse`.

### `POST /api/usuarios`

**Request**

```json
{
  "nome": "Maria Silva",
  "email": "maria@fila.local",
  "senha": "Senha@123",
  "perfil": "ATENDENTE",
  "ativo": true
}
```

**Response `201`** com `Location: /api/usuarios/{id}`.

**Erros:** `400`, `409` (`EMAIL_DUPLICADO`).

### `PUT /api/usuarios/{id}`

**Request**

```json
{
  "nome": "Maria Silva",
  "email": "maria@fila.local",
  "senha": "NovaSenha@123",
  "perfil": "ATENDENTE",
  "ativo": true
}
```

`senha` é opcional: se omitida ou em branco, a senha atual é mantida.

**Response `200`**.

### `DELETE /api/usuarios/{id}`

Desativa o usuário (exclusão lógica). **Response `204`**.

**Erros:** `409` (`OPERACAO_NAO_PERMITIDA` ao tentar desativar o próprio usuário).

---

## 7. Formato de erro (RFC 9457)

```json
{
  "type": "about:blank",
  "title": "Regra de negocio violada",
  "status": 409,
  "detail": "A fila Geral esta inativa e nao pode emitir senhas.",
  "instance": "/api/senhas",
  "timestamp": "2026-11-10T09:00:00",
  "traceId": "6f1c2a9e8b7d4c3a",
  "codigo": "FILA_INATIVA"
}
```

Erros de validação incluem o mapa `erros`:

```json
{
  "type": "about:blank",
  "title": "Erro de validacao",
  "status": 400,
  "detail": "Um ou mais campos estao invalidos.",
  "instance": "/api/usuarios",
  "timestamp": "2026-11-10T09:00:00",
  "traceId": "6f1c2a9e8b7d4c3a",
  "codigo": "ERRO_VALIDACAO",
  "erros": {
    "email": "E-mail invalido",
    "senha": "A senha deve ter entre 8 e 72 caracteres"
  }
}
```

### Códigos de erro

| `codigo` | HTTP | Significado |
| --- | --- | --- |
| `ERRO_VALIDACAO` | 400 | Campos inválidos |
| `REQUISICAO_INVALIDA` | 400 | JSON/parâmetro malformado |
| `CREDENCIAIS_INVALIDAS` | 401 | Login inválido ou usuário inativo |
| `ACESSO_NEGADO` | 403 | Perfil sem permissão |
| `SENHA_NAO_ENCONTRADA` | 404 | Senha inexistente |
| `FILA_NAO_ENCONTRADA` | 404 | Fila inexistente |
| `FILA_PADRAO_NAO_ENCONTRADA` | 404 | Prefixo padrão sem fila cadastrada |
| `ATENDIMENTO_NAO_ENCONTRADO` | 404/409 | Atendimento inexistente ou ainda não criado |
| `USUARIO_NAO_ENCONTRADO` | 404 | Usuário inexistente |
| `FILA_INATIVA` | 409 | Fila desativada |
| `FILA_VAZIA` | 409 | Nenhuma senha aguardando |
| `TRANSICAO_INVALIDA` | 409 | Transição de status não permitida |
| `FILA_NOME_DUPLICADO` | 409 | Nome de fila já usado |
| `FILA_PREFIXO_DUPLICADO` | 409 | Prefixo já usado |
| `EMAIL_DUPLICADO` | 409 | E-mail já cadastrado |
| `OPERACAO_NAO_PERMITIDA` | 409 | Ação proibida (ex.: autodesativação) |
| `CONFLITO_DADOS` | 409 | Violação de integridade |
| `ERRO_INTERNO` | 500 | Erro inesperado |

---

## 8. Observações

- Campos nulos são omitidos nas respostas (`spring.jackson.default-property-inclusion=non_null`).
- Paginação segue o padrão Spring (`page`, `size`, `sort`).
- `size` máximo é `100`.
- O token JWT é stateless e expira em 2 horas por padrão.
