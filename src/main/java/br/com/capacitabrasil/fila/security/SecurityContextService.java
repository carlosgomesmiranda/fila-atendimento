package br.com.capacitabrasil.fila.security;

import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.exception.RegraNegocioException;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Recupera o usuario autenticado a partir do SecurityContext (claim sub = e-mail).
 */
@Service
@RequiredArgsConstructor
public class SecurityContextService {

    private final UsuarioRepository usuarioRepository;

    public Optional<Usuario> usuarioAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return Optional.empty();
        }
        return usuarioRepository.findByEmailIgnoreCase(authentication.getName());
    }

    public Usuario exigirUsuarioAtual() {
        return usuarioAtual().orElseThrow(() -> new RegraNegocioException(
                "Usuario autenticado nao encontrado.", "USUARIO_NAO_AUTENTICADO"));
    }
}
