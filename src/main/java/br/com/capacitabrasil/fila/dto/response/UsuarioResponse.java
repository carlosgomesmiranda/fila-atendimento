package br.com.capacitabrasil.fila.dto.response;

import br.com.capacitabrasil.fila.entity.enums.PerfilUsuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        PerfilUsuario perfil,
        boolean ativo,
        LocalDateTime criadoEm
) {
}
