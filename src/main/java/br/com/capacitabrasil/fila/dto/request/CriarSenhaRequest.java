package br.com.capacitabrasil.fila.dto.request;

import jakarta.validation.constraints.Positive;

public record CriarSenhaRequest(
        @Positive(message = "O id da fila deve ser positivo")
        Long filaId
) {
}
