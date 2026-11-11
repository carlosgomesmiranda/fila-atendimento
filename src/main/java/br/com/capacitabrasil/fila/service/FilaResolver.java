package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.config.FilaProperties;
import br.com.capacitabrasil.fila.entity.Fila;
import br.com.capacitabrasil.fila.exception.ResourceNotFoundException;
import br.com.capacitabrasil.fila.repository.FilaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FilaResolver {

    private final FilaRepository filaRepository;
    private final FilaProperties filaProperties;

    public Fila resolver(Long filaId) {
        if (filaId != null) {
            return filaRepository.findById(filaId)
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Fila nao encontrada: " + filaId, "FILA_NAO_ENCONTRADA"));
        }
        String prefixo = Optional.ofNullable(filaProperties.prefixoPadrao()).orElse("A");
        return filaRepository.findByPrefixoIgnoreCase(prefixo)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Fila padrao nao configurada: " + prefixo, "FILA_PADRAO_NAO_ENCONTRADA"));
    }
}
