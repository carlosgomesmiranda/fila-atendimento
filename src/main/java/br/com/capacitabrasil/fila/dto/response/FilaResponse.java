package br.com.capacitabrasil.fila.dto.response;

import java.time.LocalDateTime;
import java.util.List;

public record FilaResponse(
        FilaResumoResponse fila,
        List<SenhaPainelResponse> aguardando,
        List<SenhaPainelResponse> chamadas,
        List<SenhaPainelResponse> emAtendimento,
        int totalAguardando,
        LocalDateTime atualizadoEm
) {
}
