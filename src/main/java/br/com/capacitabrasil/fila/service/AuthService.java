package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.dto.request.LoginRequest;
import br.com.capacitabrasil.fila.dto.response.LoginResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.mapper.UsuarioMapper;
import br.com.capacitabrasil.fila.repository.UsuarioRepository;
import br.com.capacitabrasil.fila.security.JwtService;
import br.com.capacitabrasil.fila.security.SecurityContextService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final JwtService jwtService;
    private final UsuarioMapper usuarioMapper;
    private final SecurityContextService securityContextService;

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.senha()));

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Usuario autenticado nao encontrado.", "USUARIO_NAO_ENCONTRADO"));

        String token = jwtService.gerarToken(usuario);
        return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpiracaoSegundos(),
                usuarioMapper.toResponse(usuario));
    }

    @Transactional(readOnly = true)
    public UsuarioResponse me() {
        return usuarioMapper.toResponse(securityContextService.exigirUsuarioAtual());
    }
}
