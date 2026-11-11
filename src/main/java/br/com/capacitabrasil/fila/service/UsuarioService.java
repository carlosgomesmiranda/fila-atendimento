package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.AtualizarUsuarioRequest;
import br.com.capacitabrasil.fila.dto.request.CriarUsuarioRequest;
import br.com.capacitabrasil.fila.dto.response.PageResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.UsuarioMapper;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final UsuarioMapper usuarioMapper;
    private final PasswordEncoder passwordEncoder;
    private final SecurityContextService securityContextService;

    @Transactional(readOnly = true)
    public PageResponse<UsuarioResponse> listar(Pageable pageable) {
        Page<Usuario> pagina = usuarioRepository.findAll(pageable);
        return PageResponse.de(pagina.map(usuarioMapper::toResponse));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse buscarPorId(Long id) {
        return usuarioMapper.toResponse(buscarEntidade(id));
    }

    @Transactional
    public UsuarioResponse criar(CriarUsuarioRequest request) {
        String email = normalizarEmail(request.email());
        if (usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Ja existe um usuario com o e-mail " + email, "EMAIL_DUPLICADO");
        }

        Usuario usuario = Usuario.builder()
                .nome(request.nome().trim())
                .email(email)
                .senha(passwordEncoder.encode(request.senha()))
                .perfil(request.perfil())
                .ativo(request.ativo() == null || request.ativo())
                .build();

        return usuarioMapper.toResponse(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, AtualizarUsuarioRequest request) {
        Usuario usuario = buscarEntidade(id);
        String email = normalizarEmail(request.email());

        if (!usuario.getEmail().equalsIgnoreCase(email)
                && usuarioRepository.existsByEmailIgnoreCase(email)) {
            throw new RegraNegocioException("Ja existe um usuario com o e-mail " + email, "EMAIL_DUPLICADO");
        }

        usuario.setNome(request.nome().trim());
        usuario.setEmail(email);
        usuario.setPerfil(request.perfil());
        usuario.setAtivo(request.ativo());
        if (request.senha() != null && !request.senha().isBlank()) {
            usuario.setSenha(passwordEncoder.encode(request.senha()));
        }

        return usuarioMapper.toResponse(usuario);
    }

    /**
     * Desativacao logica para preservar o historico de atendimentos.
     */
    @Transactional
    public void desativar(Long id) {
        Usuario atual = securityContextService.exigirUsuarioAtual();
        if (atual.getId().equals(id)) {
            throw new RegraNegocioException(
                    "Um administrador nao pode desativar o proprio usuario.", "OPERACAO_NAO_PERMITIDA");
        }
        Usuario usuario = buscarEntidade(id);
        usuario.setAtivo(false);
    }

    private Usuario buscarEntidade(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario nao encontrado: " + id, "USUARIO_NAO_ENCONTRADO"));
    }

    private String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}
