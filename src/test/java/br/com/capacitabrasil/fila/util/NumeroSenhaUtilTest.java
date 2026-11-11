package br.com.capacitabrasil.fila.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NumeroSenhaUtilTest {

    @Test
    void deveFormatarNumeroComTresDigitos() {
        assertThat(NumeroSenhaUtil.formatar("A", 1)).isEqualTo("A001");
        assertThat(NumeroSenhaUtil.formatar("p", 12)).isEqualTo("P012");
        assertThat(NumeroSenhaUtil.formatar("A", 1000)).isEqualTo("A1000");
    }

    @Test
    void deveRejeitarPrefixoInvalido() {
        assertThatThrownBy(() -> NumeroSenhaUtil.formatar(null, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NumeroSenhaUtil.formatar(" ", 1))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void deveRejeitarSequenciaInvalida() {
        assertThatThrownBy(() -> NumeroSenhaUtil.formatar("A", 0))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
