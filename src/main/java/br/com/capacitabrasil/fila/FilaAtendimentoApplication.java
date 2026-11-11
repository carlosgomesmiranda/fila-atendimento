package br.com.capacitabrasil.fila;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class FilaAtendimentoApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilaAtendimentoApplication.class, args);
    }
}
