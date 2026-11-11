package br.com.capacitabrasil.fila.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TimeConfig {

    private final String timezone;

    public TimeConfig(@Value("${app.timezone:America/Sao_Paulo}") String timezone) {
        this.timezone = timezone;
    }

    /**
     * Alinha o fuso padrao da JVM com o fuso da aplicacao.
     *
     * <p>Necessario porque {@code @CreationTimestamp}/{@code @UpdateTimestamp} do
     * Hibernate usam o fuso padrao da JVM. Sem isso, dentro de containers (UTC)
     * a auditoria ficaria divergente dos horarios de dominio gerados pelo
     * {@link #clock()}.</p>
     */
    @PostConstruct
    void configurarFusoPadrao() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(timezone)));
    }

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of(timezone));
    }
}
