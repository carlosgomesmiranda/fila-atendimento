package br.com.capacitabrasil.fila.dto.request;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record FinalizarAtendimentoRequest(
        @NotNull(message = "O id do atendimento e obrigatorio")
        @Positive(message = "O id do atendimento deve ser positivo")
        Long atendimentoId,

        @Size(max = 500, message = "A observacao deve ter no maximo 500 caracteres")
        String observacao
) {
}
