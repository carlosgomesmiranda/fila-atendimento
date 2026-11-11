package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthControllerIntegrationTest extends AbstractIntegrationTest {

    @Test
    void deveAutenticarComCredenciaisValidas() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@fila.local", "senha", "Admin@123"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tipo").value("Bearer"))
                .andExpect(jsonPath("$.usuario.email").value("admin@fila.local"))
                .andExpect(jsonPath("$.usuario.perfil").value("ADMINISTRADOR"));
    }

    @Test
    void deveRejeitarSenhaInvalida() throws Exception {
        String body = objectMapper.writeValueAsString(
                Map.of("email", "admin@fila.local", "senha", "senha-errada"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("CREDENCIAIS_INVALIDAS"));
    }

    @Test
    void deveRejeitarLoginComCorpoInvalido() throws Exception {
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"invalido\",\"senha\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.codigo").value("ERRO_VALIDACAO"))
                .andExpect(jsonPath("$.erros.email").exists())
                .andExpect(jsonPath("$.erros.senha").exists());
    }

    @Test
    void deveRejeitarRequisicaoSemToken() throws Exception {
        mockMvc.perform(get("/api/senhas"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.codigo").value("NAO_AUTENTICADO"));
    }

    @Test
    void deveRejeitarTokenInvalido() throws Exception {
        mockMvc.perform(get("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer token-invalido"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void deveRetornarUsuarioAutenticado() throws Exception {
        mockMvc.perform(get("/api/auth/me")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenAtendente())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("atendente@fila.local"))
                .andExpect(jsonPath("$.perfil").value("ATENDENTE"));
    }

    @Test
    void usuarioNaoPodeChamarProximaSenha() throws Exception {
        mockMvc.perform(post("/api/fila/proxima")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.codigo").value("ACESSO_NEGADO"));
    }

    @Test
    void usuarioNaoPodeAcessarAtendimentos() throws Exception {
        mockMvc.perform(get("/api/atendimentos")
                        .header(HttpHeaders.AUTHORIZATION, bearer(tokenUsuario())))
                .andExpect(status().isForbidden());
    }
}
