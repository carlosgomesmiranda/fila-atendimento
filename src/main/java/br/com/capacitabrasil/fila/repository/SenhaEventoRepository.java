package br.com.capacitabrasil.fila.repository;

import br.com.capacitabrasil.fila.entity.SenhaEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SenhaEventoRepository extends JpaRepository<SenhaEvento, Long> {

    List<SenhaEvento> findBySenhaIdOrderByOcorridoEmAsc(Long senhaId);
}
