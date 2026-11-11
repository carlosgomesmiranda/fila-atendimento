package br.com.capacitabrasil.fila.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.fila")
public record FilaProperties(
        String prefixoPadrao
) {
}
