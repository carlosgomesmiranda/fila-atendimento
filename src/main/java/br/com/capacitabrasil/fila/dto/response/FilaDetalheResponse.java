package br.com.capacitabrasil.fila.dto.response;

import java.time.LocalDateTime;

public record FilaDetalheResponse(
        Long id,
        String nome,
        String prefixo,
        String descricao,
        boolean ativa,
        LocalDateTime criadoEm,
        LocalDateTime atualizadoEm
) {
}
