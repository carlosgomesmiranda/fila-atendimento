package br.com.capacitabrasil.fila.repository;

import br.com.capacitabrasil.fila.entity.Fila;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FilaRepository extends JpaRepository<Fila, Long> {

    Optional<Fila> findByPrefixoIgnoreCase(String prefixo);

    List<Fila> findByAtivaTrueOrderByNomeAsc();

    boolean existsByPrefixoIgnoreCase(String prefixo);

    boolean existsByNomeIgnoreCase(String nome);

    /**
     * Bloqueia a linha da fila para gerar a proxima sequencia sem concorrencia.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select f from Fila f where f.id = :id")
    Optional<Fila> findByIdParaAtualizacao(@Param("id") Long id);
}
