package br.com.capacitabrasil.fila.util;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;

public final class DuracaoUtil {

    private DuracaoUtil() {
    }

    public static long segundosEntre(LocalDateTime inicio, LocalDateTime fim) {
        return Duration.between(inicio, fim).toSeconds();
    }

    public static double arredondar(double valor, int casasDecimais) {
        if (Double.isNaN(valor) || Double.isInfinite(valor)) {
            return 0.0;
        }
        return BigDecimal.valueOf(valor)
                .setScale(casasDecimais, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
