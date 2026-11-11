package br.com.capacitabrasil.fila.dto.response;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;

import java.time.LocalDateTime;

public record AtendimentoResponse(
        Long id,
        Long senhaId,
        String numeroSenha,
        StatusSenha statusSenha,
        LocalDateTime inicio,
        LocalDateTime fim,
        String guiche,
        String observacao,
        Long atendenteId,
        String atendenteNome,
        Long duracaoSegundos
) {
}
