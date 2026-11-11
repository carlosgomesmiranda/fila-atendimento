package br.com.capacitabrasil.fila.service;

import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.SenhaEvento;
import br.com.capacitabrasil.fila.entity.Usuario;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.repository.SenhaEventoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SenhaEventoService {

    private final SenhaEventoRepository senhaEventoRepository;
    private final Clock clock;

    @Transactional
    public void registrar(Senha senha,
                          StatusSenha statusAnterior,
                          StatusSenha statusNovo,
                          Usuario usuario,
                          String detalhe) {
        SenhaEvento evento = SenhaEvento.builder()
                .senha(senha)
                .statusAnterior(statusAnterior)
                .statusNovo(statusNovo)
                .ocorridoEm(LocalDateTime.now(clock))
                .usuario(usuario)
                .detalhe(detalhe)
                .build();
        senhaEventoRepository.save(evento);
    }
}
