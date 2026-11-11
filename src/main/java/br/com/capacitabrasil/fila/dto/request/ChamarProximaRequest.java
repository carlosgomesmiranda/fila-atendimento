package br.com.capacitabrasil.fila.dto.request;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record ChamarProximaRequest(
        @Positive(message = "O id da fila deve ser positivo")
        Long filaId,

        @Size(max = 20, message = "O guiche deve ter no maximo 20 caracteres")
        String guiche
) {
}
