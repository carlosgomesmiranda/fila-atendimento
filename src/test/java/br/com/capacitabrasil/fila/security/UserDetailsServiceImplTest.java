package br.com.capacitabrasil.fila.security;

import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Test
    void deveCarregarUsuarioAtivoComRole() {
        Usuario usuario = Usuario.builder()
                .id(1L)
                .nome("Atendente")
                .email("atendente@fila.local")
                .senha("hash")
                .perfil(PerfilUsuario.ATENDENTE)
                .ativo(true)
                .build();
        when(usuarioRepository.findByEmailIgnoreCase("atendente@fila.local"))
                .thenReturn(Optional.of(usuario));

        UserDetails userDetails = userDetailsService.loadUserByUsername("atendente@fila.local");

        assertThat(userDetails.getUsername()).isEqualTo("atendente@fila.local");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .containsExactly("ROLE_ATENDENTE");
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void deveRejeitarUsuarioInexistente() {
        when(usuarioRepository.findByEmailIgnoreCase("x@fila.local")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("x@fila.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    @Test
    void deveRejeitarUsuarioInativo() {
        Usuario usuario = Usuario.builder()
                .email("inativo@fila.local")
                .senha("hash")
                .perfil(PerfilUsuario.USUARIO)
                .ativo(false)
                .build();
        when(usuarioRepository.findByEmailIgnoreCase("inativo@fila.local"))
                .thenReturn(Optional.of(usuario));

        assertThatThrownBy(() -> userDetailsService.loadUserByUsername("inativo@fila.local"))
                .isInstanceOf(UsernameNotFoundException.class);
    }
}
