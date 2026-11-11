package br.com.capacitabrasil.fila.repository;

import br.com.capacitabrasil.fila.entity.Atendimento;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AtendimentoRepository extends JpaRepository<Atendimento, Long>, JpaSpecificationExecutor<Atendimento> {

    @Override
    @EntityGraph(attributePaths = {"senha", "senha.fila", "atendente"})
    Optional<Atendimento> findById(Long id);

    @EntityGraph(attributePaths = {"senha", "senha.fila", "atendente"})
    Optional<Atendimento> findBySenhaId(Long senhaId);

    @Override
    @EntityGraph(attributePaths = {"senha", "senha.fila", "atendente"})
    Page<Atendimento> findAll(Specification<Atendimento> spec, Pageable pageable);

    boolean existsBySenhaId(Long senhaId);
}
