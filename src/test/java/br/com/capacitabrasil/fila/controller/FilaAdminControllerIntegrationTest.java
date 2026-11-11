package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class FilaAdminControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String NOVA_FILA = """
            {"nome":"Exames","prefixo":"E","descricao":"Fila de exames","ativa":true}
            """;

    @Test
    void deveCriarAtualizarEDesativarFila() throws Exception {
        String tokenAdmin = tokenAdmin();

        MvcResult criada = mockMvc.perform(post("/api/filas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA_FILA))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.prefixo").value("E"))
                .andExpect(jsonPath("$.ativa").value(true))
                .andReturn();
        long id = objectMapper.readTree(criada.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/filas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Exames Especiais","prefixo":"X","descricao":"Atualizada","ativa":true}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Exames Especiais"))
                .andExpect(jsonPath("$.prefixo").value("X"));

        mockMvc.perform(delete("/api/filas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/filas/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.ativa").value(false));
    }

    @Test
    void naoDeveCriarFilaComPrefixoDuplicado() throws Exception {
        mockMvc.perform(post("/api/filas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outra","prefixo":"A","ativa":true}
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("FILA_PREFIXO_DUPLICADO"));
    }

    @Test
    void naoDeveCriarFilaComPrefixoInvalido() throws Exception {
        mockMvc.perform(post("/api/filas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"Outra","prefixo":"AB","ativa":true}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERRO_VALIDACAO"));
    }

    @Test
    void usuarioComumNaoPodeCriarFila() throws Exception {
        mockMvc.perform(post("/api/filas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVA_FILA))
                .andExpect(status().isForbidden());
    }

    @Test
    void usuarioComumPodeListarFilas() throws Exception {
        mockMvc.perform(get("/api/filas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nome").value("Geral"))
                .andExpect(jsonPath("$[1].nome").value("Prioritaria"));
    }
}
