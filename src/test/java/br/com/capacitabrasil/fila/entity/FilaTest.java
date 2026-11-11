package br.com.capacitabrasil.fila.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class FilaTest {

    @Test
    void deveIniciarSequenciaNoPrimeiroDia() {
        Fila fila = Fila.builder().nome("Geral").prefixo("A").ativa(true).build();
        LocalDate hoje = LocalDate.of(2026, 11, 10);

        int sequencia = fila.proximaSequencia(hoje);

        assertThat(sequencia).isEqualTo(1);
        assertThat(fila.getDataUltimaSequencia()).isEqualTo(hoje);
        assertThat(fila.getUltimaSequencia()).isEqualTo(1);
    }

    @Test
    void deveIncrementarSequenciaNoMesmoDia() {
        LocalDate hoje = LocalDate.of(2026, 11, 10);
        Fila fila = Fila.builder()
                .nome("Geral")
                .prefixo("A")
                .ativa(true)
                .dataUltimaSequencia(hoje)
                .ultimaSequencia(5)
                .build();

        int sequencia = fila.proximaSequencia(hoje);

        assertThat(sequencia).isEqualTo(6);
        assertThat(fila.getUltimaSequencia()).isEqualTo(6);
    }

    @Test
    void deveReiniciarSequenciaEmOutroDia() {
        Fila fila = Fila.builder()
                .nome("Geral")
                .prefixo("A")
                .ativa(true)
                .dataUltimaSequencia(LocalDate.of(2026, 11, 9))
                .ultimaSequencia(42)
                .build();

        int sequencia = fila.proximaSequencia(LocalDate.of(2026, 11, 10));

        assertThat(sequencia).isEqualTo(1);
        assertThat(fila.getUltimaSequencia()).isEqualTo(1);
        assertThat(fila.getDataUltimaSequencia()).isEqualTo(LocalDate.of(2026, 11, 10));
    }
}
