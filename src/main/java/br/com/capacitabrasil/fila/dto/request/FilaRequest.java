package br.com.capacitabrasil.fila.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record FilaRequest(
        @NotBlank(message = "O nome da fila e obrigatorio")
        @Size(max = 80, message = "O nome deve ter no maximo 80 caracteres")
        String nome,

        @NotBlank(message = "O prefixo e obrigatorio")
        @Size(max = 2, message = "O prefixo deve ter no maximo 2 caracteres")
        @Pattern(regexp = "[A-Za-z]", message = "O prefixo deve conter uma letra")
        String prefixo,

        @Size(max = 255, message = "A descricao deve ter no maximo 255 caracteres")
        String descricao,

        Boolean ativa
) {
}
