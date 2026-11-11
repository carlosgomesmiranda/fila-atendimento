package br.com.capacitabrasil.fila.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
public abstract class AbstractIntegrationTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    protected String tokenAdmin() throws Exception {
        return login("admin@fila.local", "Admin@123");
    }

    protected String tokenAtendente() throws Exception {
        return login("atendente@fila.local", "Atendente@123");
    }

    protected String tokenUsuario() throws Exception {
        return login("usuario@fila.local", "Usuario@123");
    }

    protected String login(String email, String senha) throws Exception {
        String body = objectMapper.writeValueAsString(Map.of("email", email, "senha", senha));
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("token").asText();
    }

    protected String bearer(String token) {
        return "Bearer " + token;
    }

    protected String authHeader() {
        return HttpHeaders.AUTHORIZATION;
    }

    protected long criarSenha(String token) throws Exception {
        return criarSenha(token, null);
    }

    protected long criarSenha(String token, Long filaId) throws Exception {
        String body = filaId == null ? "{}" : objectMapper.writeValueAsString(Map.of("filaId", filaId));
        MvcResult result = mockMvc.perform(post("/api/senhas")
                        .header(HttpHeaders.AUTHORIZATION, bearer(token))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();
    }
}
