package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.matchesPattern;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SenhaControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void deveGerarSenhaComNumeracaoSequencial() throws Exception {
        mockMvc.perform(post("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists(HttpHeaders.LOCATION))
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.numero").value(matchesPattern("A\\d{3}")))
                .andExpect(jsonPath("$.status").value("AGUARDANDO"))
                .andExpect(jsonPath("$.fila.prefixo").value("A"))
                .andExpect(jsonPath("$.posicao").value(1))
                .andExpect(jsonPath("$.pessoasNaFrente").value(0));
    }

    @Test
    void deveGerarSenhaEmFilaEspecifica() throws Exception {
        mockMvc.perform(post("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filaId\":2}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(matchesPattern("P\\d{3}")))
                .andExpect(jsonPath("$.fila.nome").value("Prioritaria"));
    }

    @Test
    void deveConsultarSenhaPorIdComPosicao() throws Exception {
        long id = criarSenha(tokenUsuario());

        mockMvc.perform(get("/api/senhas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.posicao").value(1));
    }

    @Test
    void deveConsultarUltimaSenhaEmitida() throws Exception {
        criarSenha(tokenUsuario());
        criarSenha(tokenUsuario());

        mockMvc.perform(get("/api/senhas/ultima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.numero").value("A002"));
    }

    @Test
    void deveListarSenhasComFiltro() throws Exception {
        criarSenha(tokenUsuario());

        mockMvc.perform(get("/api/senhas")
                        .param("status", "AGUARDANDO")
                        .param("filaId", "1")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(1))
                .andExpect(jsonPath("$.conteudo[0].numero").value("A001"));
    }

    @Test
    void deveRetornar404QuandoNaoHaSenhaNoDia() throws Exception {
        mockMvc.perform(get("/api/senhas/ultima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("SENHA_NAO_ENCONTRADA"));
    }

    @Test
    void deveCancelarSenhaSemRetornarAFila() throws Exception {
        long id = criarSenha(tokenUsuario());

        mockMvc.perform(post("/api/senhas/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"));

        mockMvc.perform(get("/api/senhas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELADA"))
                .andExpect(jsonPath("$.posicao").doesNotExist());
    }

    @Test
    void naoDeveCancelarSenhaDuasVezes() throws Exception {
        long id = criarSenha(tokenUsuario());
        String token = tokenAtendente();

        mockMvc.perform(post("/api/senhas/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/senhas/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(token)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("TRANSICAO_INVALIDA"));
    }

    @Test
    void usuarioNaoPodeCancelarSenha() throws Exception {
        long id = criarSenha(tokenUsuario());

        mockMvc.perform(post("/api/senhas/{id}/cancelar", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveRetornar404ParaFilaInexistente() throws Exception {
        mockMvc.perform(post("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filaId\":9999}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("FILA_NAO_ENCONTRADA"));
    }

    @Test
    void deveValidarFilaIdNegativo() throws Exception {
        mockMvc.perform(post("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"filaId\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERRO_VALIDACAO"));
    }
}
