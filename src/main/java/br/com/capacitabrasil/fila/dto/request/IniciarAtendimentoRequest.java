package br.com.capacitabrasil.fila.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record IniciarAtendimentoRequest(
        @NotNull(message = "O id da senha e obrigatorio")
        @Positive(message = "O id da senha deve ser positivo")
        Long senhaId,

        @Size(max = 20, message = "O guiche deve ter no maximo 20 caracteres")
        String guiche
) {
}
