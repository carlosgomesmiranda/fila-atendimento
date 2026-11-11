package br.com.capacitabrasil.fila.util;

import java.util.Locale;

public final class NumeroSenhaUtil {

    private NumeroSenhaUtil() {
    }

    /**
     * Formata o numero da senha no padrao prefixo + sequencia com 3 digitos (RN001).
     * Ex.: A + 1 -> A001. Acima de 999 o numero cresce naturalmente (A1000).
     */
    public static String formatar(String prefixo, int sequencia) {
        if (prefixo == null || prefixo.isBlank()) {
            throw new IllegalArgumentException("O prefixo da fila e obrigatorio.");
        }
        if (sequencia <= 0) {
            throw new IllegalArgumentException("A sequencia deve ser positiva.");
        }
        return "%s%03d".formatted(prefixo.trim().toUpperCase(Locale.ROOT), sequencia);
    }
}
