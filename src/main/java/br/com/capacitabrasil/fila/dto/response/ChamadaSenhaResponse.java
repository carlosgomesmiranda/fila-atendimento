package br.com.capacitabrasil.fila.dto.response;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;

import java.time.LocalDateTime;

public record ChamadaSenhaResponse(
        Long senhaId,
        String numero,
        StatusSenha status,
        LocalDateTime horaChamada,
        Long atendimentoId,
        String guiche,
        long aguardandoRestantes
) {
}
