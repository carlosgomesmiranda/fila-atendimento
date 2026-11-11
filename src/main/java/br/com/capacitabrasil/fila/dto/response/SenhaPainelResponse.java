package br.com.capacitabrasil.fila.dto.response;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;

import java.time.LocalDateTime;

public record SenhaPainelResponse(
        Long id,
        String numero,
        StatusSenha status,
        LocalDateTime dataGeracao,
        LocalDateTime horaChamada,
        LocalDateTime horaInicioAtendimento
) {
}
