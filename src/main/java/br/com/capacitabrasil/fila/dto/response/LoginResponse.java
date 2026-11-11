package br.com.capacitabrasil.fila.dto.response;

public record LoginResponse(
        String token,
        String tipo,
        long expiraEmSegundos,
        UsuarioResponse usuario
) {
}
