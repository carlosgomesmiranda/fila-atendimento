package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.LoginRequest;
import br.com.capacitabrasil.fila.dto.response.LoginResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.UsuarioMapper;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import br.com.capacitabrasil.fila.security.JwtService;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AuthenticationManager authenticationManager;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private JwtService jwtService;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private SecurityContextService securityContextService;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthService(authenticationManager, usuarioRepository,
                jwtService, usuarioMapper, securityContextService);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .nome("Admin")
                .email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR)
                .ativo(true)
                .build();
    }

    @Test
    void deveAutenticarERetornarToken() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin@fila.local", null);
        UsuarioResponse usuarioResponse = new UsuarioResponse(1L, "Admin", "admin@fila.local",
                PerfilUsuario.ADMINISTRADOR, true, null);

        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByEmailIgnoreCase("admin@fila.local"))
                .thenReturn(Optional.of(usuario()));
        when(jwtService.gerarToken(any(Usuario.class))).thenReturn("jwt-token");
        when(jwtService.getExpiracaoSegundos()).thenReturn(7200L);
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(usuarioResponse);

        LoginResponse response = authService.login(new LoginRequest("admin@fila.local", "Admin@123"));

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tipo()).isEqualTo("Bearer");
        assertThat(response.expiraEmSegundos()).isEqualTo(7200L);
        assertThat(response.usuario()).isSameAs(usuarioResponse);
    }

    @Test
    void devePropagarFalhaDeAutenticacao() {
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Credenciais invalidas"));

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("admin@fila.local", "errada")))
                .isInstanceOf(BadCredentialsException.class);
    }

    @Test
    void deveFalharQuandoUsuarioAutenticadoNaoExiste() {
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                "admin@fila.local", null);
        when(authenticationManager.authenticate(any())).thenReturn(authentication);
        when(usuarioRepository.findByEmailIgnoreCase("admin@fila.local"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(
                new LoginRequest("admin@fila.local", "Admin@123")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void deveRetornarUsuarioAutenticado() {
        Usuario usuario = usuario();
        UsuarioResponse response = new UsuarioResponse(1L, "Admin", "admin@fila.local",
                PerfilUsuario.ADMINISTRADOR, true, null);

        when(securityContextService.exigirUsuarioAtual()).thenReturn(usuario);
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        assertThat(authService.me()).isSameAs(response);
    }
}
