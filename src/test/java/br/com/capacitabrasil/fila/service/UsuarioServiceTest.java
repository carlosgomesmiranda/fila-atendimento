package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.AtualizarUsuarioRequest;
import br.com.capacitabrasil.fila.dto.request.CriarUsuarioRequest;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.UsuarioMapper;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private UsuarioMapper usuarioMapper;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityContextService securityContextService;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioService(
                usuarioRepository, usuarioMapper, passwordEncoder, securityContextService);
    }

    private Usuario usuario() {
        return Usuario.builder()
                .id(1L)
                .nome("Usuario")
                .email("usuario@fila.local")
                .senha("hash-antigo")
                .perfil(PerfilUsuario.USUARIO)
                .ativo(true)
                .build();
    }

    @Test
    void deveCriarUsuarioComSenhaCriptografada() {
        UsuarioResponse response = new UsuarioResponse(10L, "Novo", "novo@fila.local",
                PerfilUsuario.ATENDENTE, true, null);

        when(usuarioRepository.existsByEmailIgnoreCase("novo@fila.local")).thenReturn(false);
        when(passwordEncoder.encode("Senha@123")).thenReturn("hash-novo");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(10L);
            return u;
        });
        when(usuarioMapper.toResponse(any(Usuario.class))).thenReturn(response);

        UsuarioResponse criado = usuarioService.criar(new CriarUsuarioRequest(
                " Novo ", "NOVO@Fila.local ", "Senha@123", PerfilUsuario.ATENDENTE, null));

        assertThat(criado).isSameAs(response);
        verify(usuarioRepository).save(any(Usuario.class));
    }

    @Test
    void naoDeveCriarUsuarioComEmailDuplicado() {
        when(usuarioRepository.existsByEmailIgnoreCase("novo@fila.local")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.criar(new CriarUsuarioRequest(
                "Novo", "novo@fila.local", "Senha@123", PerfilUsuario.USUARIO, true)))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("e-mail");
    }

    @Test
    void deveAtualizarUsuarioSemTrocarSenha() {
        Usuario usuario = usuario();
        UsuarioResponse response = new UsuarioResponse(1L, "Atualizado", "usuario@fila.local",
                PerfilUsuario.ATENDENTE, true, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        UsuarioResponse atualizado = usuarioService.atualizar(1L, new AtualizarUsuarioRequest(
                "Atualizado", "usuario@fila.local", null, PerfilUsuario.ATENDENTE, true));

        assertThat(atualizado).isSameAs(response);
        assertThat(usuario.getNome()).isEqualTo("Atualizado");
        assertThat(usuario.getPerfil()).isEqualTo(PerfilUsuario.ATENDENTE);
        assertThat(usuario.getSenha()).isEqualTo("hash-antigo");
    }

    @Test
    void deveAtualizarSenhaQuandoInformada() {
        Usuario usuario = usuario();
        UsuarioResponse response = new UsuarioResponse(1L, "Usuario", "usuario@fila.local",
                PerfilUsuario.USUARIO, true, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(passwordEncoder.encode("NovaSenha@123")).thenReturn("hash-novo");
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        usuarioService.atualizar(1L, new AtualizarUsuarioRequest(
                "Usuario", "usuario@fila.local", "NovaSenha@123", PerfilUsuario.USUARIO, true));

        assertThat(usuario.getSenha()).isEqualTo("hash-novo");
    }

    @Test
    void naoDeveAtualizarParaEmailDeOutroUsuario() {
        Usuario usuario = usuario();
        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.existsByEmailIgnoreCase("outro@fila.local")).thenReturn(true);

        assertThatThrownBy(() -> usuarioService.atualizar(1L, new AtualizarUsuarioRequest(
                "Usuario", "outro@fila.local", null, PerfilUsuario.USUARIO, true)))
                .isInstanceOf(RegraNegocioException.class);
    }

    @Test
    void naoDeveDesativarOProprioUsuario() {
        Usuario admin = Usuario.builder().id(1L).email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR).ativo(true).build();
        when(securityContextService.exigirUsuarioAtual()).thenReturn(admin);

        assertThatThrownBy(() -> usuarioService.desativar(1L))
                .isInstanceOf(RegraNegocioException.class)
                .hasMessageContaining("proprio");
    }

    @Test
    void deveDesativarOutroUsuario() {
        Usuario admin = Usuario.builder().id(1L).email("admin@fila.local")
                .perfil(PerfilUsuario.ADMINISTRADOR).ativo(true).build();
        Usuario usuario = usuario();

        when(securityContextService.exigirUsuarioAtual()).thenReturn(admin);
        when(usuarioRepository.findById(2L)).thenReturn(Optional.of(usuario));

        usuarioService.desativar(2L);

        assertThat(usuario.isAtivo()).isFalse();
    }

    @Test
    void deveListarUsuariosPaginados() {
        Pageable pageable = PageRequest.of(0, 20);
        Usuario usuario = usuario();
        UsuarioResponse response = new UsuarioResponse(1L, "Usuario", "usuario@fila.local",
                PerfilUsuario.USUARIO, true, null);

        when(usuarioRepository.findAll(pageable))
                .thenReturn(new PageImpl<>(List.of(usuario), pageable, 1));
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        PageResponse<UsuarioResponse> pagina = usuarioService.listar(pageable);

        assertThat(pagina.conteudo()).containsExactly(response);
    }

    @Test
    void deveBuscarUsuarioPorId() {
        Usuario usuario = usuario();
        UsuarioResponse response = new UsuarioResponse(1L, "Usuario", "usuario@fila.local",
                PerfilUsuario.USUARIO, true, null);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioMapper.toResponse(usuario)).thenReturn(response);

        assertThat(usuarioService.buscarPorId(1L)).isSameAs(response);
    }

    @Test
    void deveFalharAoBuscarUsuarioInexistente() {
        when(usuarioRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
