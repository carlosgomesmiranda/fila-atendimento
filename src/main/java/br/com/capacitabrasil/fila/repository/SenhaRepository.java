package br.com.capacitabrasil.fila.repository;

import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface SenhaRepository extends JpaRepository<Senha, Long>, JpaSpecificationExecutor<Senha> {

    @Override
    @EntityGraph(attributePaths = "fila")
    Optional<Senha> findById(Long id);

    @Override
    @EntityGraph(attributePaths = "fila")
    Page<Senha> findAll(org.springframework.data.jpa.domain.Specification<Senha> spec, Pageable pageable);

    /**
     * Busca a proxima senha da fila respeitando FIFO (RN002), com lock pessimista
     * para impedir que dois atendentes chamem a mesma senha.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            select s from Senha s
            where s.fila.id = :filaId
              and s.status = :status
            order by s.dataGeracao asc, s.sequencia asc
            """)
    List<Senha> buscarPrimeiras(@Param("filaId") Long filaId,
                                @Param("status") StatusSenha status,
                                Pageable pageable);

    Optional<Senha> findTopByFilaIdAndDataReferenciaOrderBySequenciaDesc(Long filaId, LocalDate dataReferencia);

    long countByFilaIdAndStatus(Long filaId, StatusSenha status);

    long countByFilaIdAndStatusAndDataReferencia(Long filaId, StatusSenha status, LocalDate dataReferencia);

    long countByStatusAndDataReferencia(StatusSenha status, LocalDate dataReferencia);

    List<Senha> findByFilaIdAndDataReferenciaOrderByDataGeracaoAscSequenciaAsc(Long filaId, LocalDate dataReferencia);

    List<Senha> findByFilaIdAndStatusOrderByDataGeracaoAscSequenciaAsc(Long filaId, StatusSenha status);

    List<Senha> findByFilaIdAndStatusOrderByHoraChamadaAsc(Long filaId, StatusSenha status);

    List<Senha> findByFilaIdAndStatusOrderByHoraInicioAtendimentoAsc(Long filaId, StatusSenha status);

    /**
     * Quantidade de senhas aguardando que estao a frente da senha informada.
     * A ordem de geracao acompanha o id (identity), garantindo FIFO inclusive
     * entre dias e evitando comparacoes de precisao de timestamp.
     */
    @Query("""
            select count(s) from Senha s
            where s.fila.id = :filaId
              and s.status = :status
              and s.id < :id
            """)
    long contarAntes(@Param("filaId") Long filaId,
                     @Param("status") StatusSenha status,
                     @Param("id") Long id);
}
