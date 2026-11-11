package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.support.AbstractIntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UsuarioControllerIntegrationTest extends AbstractIntegrationTest {

    private static final String NOVO_USUARIO = """
            {
              "nome": "Novo Atendente",
              "email": "novo@fila.local",
              "senha": "Senha@123",
              "perfil": "ATENDENTE",
              "ativo": true
            }
            """;

    @Test
    void deveCriarUsuarioEpermitirLogin() throws Exception {
        String tokenAdmin = tokenAdmin();

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_USUARIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("novo@fila.local"))
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"))
                .andExpect(jsonPath("$.senha").doesNotExist());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "novo@fila.local", "senha", "Senha@123"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void naoDeveCriarUsuarioComEmailDuplicado() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_USUARIO))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_USUARIO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("EMAIL_DUPLICADO"));
    }

    @Test
    void deveAtualizarEDesativarUsuario() throws Exception {
        String tokenAdmin = tokenAdmin();

        MvcResult criado = mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(NOVO_USUARIO))
                .andExpect(status().isCreated())
                .andReturn();
        long id = objectMapper.readTree(criado.getResponse().getContentAsString()).get("id").asLong();

        mockMvc.perform(put("/api/usuarios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "nome": "Atendente Atualizado",
                                  "email": "novo@fila.local",
                                  "perfil": "ATENDENTE",
                                  "ativo": true
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nome").value("Atendente Atualizado"));

        mockMvc.perform(delete("/api/usuarios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                Map.of("email", "novo@fila.local", "senha", "Senha@123"))))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void naoDeveDesativarOProprioAdministrador() throws Exception {
        String tokenAdmin = tokenAdmin();

        MvcResult me = mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isOk())
                .andReturn();
        JsonNode json = objectMapper.readTree(me.getResponse().getContentAsString());
        long id = json.get("id").asLong();

        mockMvc.perform(delete("/api/usuarios/{id}", id)
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.codigo").value("OPERACAO_NAO_PERMITIDA"));
    }

    @Test
    void atendenteNaoPodeGerenciarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente())))
                .andExpect(status().isForbidden());
    }

    @Test
    void deveValidarDadosDoUsuario() throws Exception {
        mockMvc.perform(post("/api/usuarios")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"nome":"","email":"invalido","senha":"123","perfil":null}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERRO_VALIDACAO"))
                .andExpect(jsonPath("$.erros.nome").exists())
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists())
                .andExpect(jsonPath("$.erros.perfil").exists());
    }

    @Test
    void deveListarUsuarios() throws Exception {
        mockMvc.perform(get("/api/usuarios")
                        .param("size", "50")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAdmin())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElementos").value(3));
    }
}
