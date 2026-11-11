package br.com.capacitabrasil.fila.security;

import br.com.capacitabrasil.fila.config.JwtProperties;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import io.jsonwebtoken.ExpiredJwtException;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtServiceTest {

    private static final String SECRET =
            "dGVzdGUtc2VjcmV0LWtleS1wYXJhLXRlc3Rlcy1kZS1pbnRlZ3JhY2FvLTEyMzQ1Njc4OTA=";

    private final JwtService jwtService = new JwtService(
            new JwtProperties(SECRET, 60_000, "fila-test"));

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .nome("Administrador")
                .email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR)
                .ativo(true)
                .build();
    }

    @Test
    void deveGerarTokenEextrairEmail() {
        String token = jwtService.gerarToken(usuario());

        assertThat(token).isNotBlank();
        assertThat(jwtService.extrairEmail(token)).isEqualTo("admin@fila.local");
        assertThat(jwtService.getExpiracaoSegundos()).isEqualTo(60);
    }

    @Test
    void tokenDeveSerValidoParaOUsuario() {
        String token = jwtService.gerarToken(usuario());
        UserDetails userDetails = User.withUsername("admin@fila.local")
                .password("x")
                .authorities("ROLE_ADMINISTRADOR")
                .build();

        assertThat(jwtService.tokenValido(token, userDetails)).isTrue();
    }

    @Test
    void tokenNaoDeveSerValidoParaOutroUsuario() {
        String token = jwtService.gerarToken(usuario());
        UserDetails outro = User.withUsername("outro@fila.local")
                .password("x")
                .authorities("ROLE_USUARIO")
                .build();

        assertThat(jwtService.tokenValido(token, outro)).isFalse();
    }

    @Test
    void deveRejeitarTokenExpirado() {
        JwtService servicoExpirado = new JwtService(
                new JwtProperties(SECRET, -1_000, "fila-test"));
        String token = servicoExpirado.gerarToken(usuario());

        assertThatThrownBy(() -> servicoExpirado.extrairEmail(token))
                .isInstanceOf(ExpiredJwtException.class);
    }

    @Test
    void deveRejeitarTokenInvalido() {
        assertThatThrownBy(() -> jwtService.extrairEmail("token-invalido"))
                .isInstanceOf(RuntimeException.class);
    }
}
