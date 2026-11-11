package br.com.capacitabrasil.fila.dto.response;

import java.time.LocalDate;

public record EstatisticasResponse(
        LocalDate data,
        long totalEmitidas,
        long aguardando,
        long chamadas,
        long emAtendimento,
        long atendidas,
        long canceladas,
        double tempoMedioEsperaSegundos,
        double tempoMedioAtendimentoSegundos,
        String ultimaSenhaEmitida,
        String ultimaSenhaChamada
) {
}
