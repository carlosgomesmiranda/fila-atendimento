package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilaControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void deveExecutarFluxoCompletoDeAtendimento() throws Exception {
        String tokenUsuario = tokenUsuario();
        String tokenAtendente = tokenAtendente();
        long senhaId = criarSenha(tokenUsuario);

        mockMvc.perform(get("/api/fila")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalAguardando").value(1))
                .andExpect(jsonPath("$.aguardando[0].numero").value("A001"));

        MvcResult chamada = mockMvc.perform(post("/api/fila/proxima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guiche\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("A001"))
                .andExpect(jsonPath("$.status").value("CHAMADA"))
                .andExpect(jsonPath("$.guiche").value("3"))
                .andExpect(jsonPath("$.aguardandoRestantes").value(0))
                .andReturn();

        JsonNode chamadaJson = objectMapper.readTree(chamada.getResponse().getContentAsString());
        long atendimentoId = chamadaJson.get("atendimentoId").asLong();

        mockMvc.perform(get("/api/fila")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.chamadas[0].numero").value("A001"))
                .andExpect(jsonPath("$.totalAguardando").value(0));

        mockMvc.perform(post("/api/atendimentos/iniciar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaId\":" + senhaId + ",\"guiche\":\"3\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSenha").value("A001"))
                .andExpect(jsonPath("$.statusSenha").value("EM_ATENDIMENTO"))
                .andExpect(jsonPath("$.inicio").isNotEmpty());

        mockMvc.perform(post("/api/atendimentos/finalizar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"atendimentoId\":" + atendimentoId + ",\"observacao\":\"Concluido\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusSenha").value("ATENDIDA"))
                .andExpect(jsonPath("$.fim").isNotEmpty())
                .andExpect(jsonPath("$.duracaoSegundos").isNumber())
                .andExpect(jsonPath("$.observacao").value("Concluido"));

        mockMvc.perform(get("/api/fila/estatisticas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalEmitidas").value(1))
                .andExpect(jsonPath("$.atendidas").value(1))
                .andExpect(jsonPath("$.ultimaSenhaEmitida").value("A001"))
                .andExpect(jsonPath("$.ultimaSenhaChamada").value("A001"));

        mockMvc.perform(get("/api/atendimentos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].id").value(atendimentoId));

        mockMvc.perform(get("/api/atendimentos/{id}", atendimentoId)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numeroSenha").value("A001"));
    }

    @Test
    void deveFalharAoChamarFilaVazia() throws Exception {
        mockMvc.perform(post("/api/fila/proxima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FILA_VAZIA"));
    }

    @Test
    void naoDeveIniciarAtendimentoSemChamada() throws Exception {
        long senhaId = criarSenha(tokenUsuario());

        mockMvc.perform(post("/api/atendimentos/iniciar")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"senhaId\":" + senhaId + "}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("ATENDIMENTO_NAO_ENCONTRADO"));
    }

    @Test
    void deveListarAtendimentosComFiltroDeGuiche() throws Exception {
        String tokenAtendente = tokenAtendente();
        criarSenha(tokenUsuario());
        mockMvc.perform(post("/api/fila/proxima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"guiche\":\"7\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/atendimentos")
                        .param("guiche", "7")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1));
    }
}
