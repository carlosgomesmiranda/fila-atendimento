package br.com.capacitabrasil.fila.dto.response;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;

import java.time.LocalDateTime;

public record SenhaResponse(
        Long id,
        String numero,
        StatusSenha status,
        FilaResumoResponse fila,
        LocalDateTime dataGeracao,
        LocalDateTime horaChamada,
        LocalDateTime horaInicioAtendimento,
        LocalDateTime horaFimAtendimento,
        Integer posicao,
        Long pessoasNaFrente
) {
}
