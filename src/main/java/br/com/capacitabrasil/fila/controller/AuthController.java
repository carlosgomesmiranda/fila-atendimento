package br.com.capacitabrasil.fila.controller;

import br.com.capacitabrasil.fila.dto.request.LoginRequest;
import br.com.capacitabrasil.fila.dto.response.LoginResponse;
import br.com.capacitabrasil.fila.dto.response.UsuarioResponse;
import br.com.capacitabrasil.fila.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticacao", description = "Login e dados do usuario autenticado")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @SecurityRequirements
    @Operation(summary = "Autentica o usuario e retorna um token JWT")
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "Retorna os dados do usuario autenticado")
    public ResponseEntity<UsuarioResponse> me() {
        return ResponseEntity.ok(authService.me());
    }
}
