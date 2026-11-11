package br.com.capacitabrasil.fila.repository.specification;

import br.com.capacitabrasil.fila.entity.Atendimento;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class AtendimentoSpecification {

    private AtendimentoSpecification() {
    }

    public static Specification<Atendimento> comFiltros(LocalDate dataInicio,
                                                        LocalDate dataFim,
                                                        String guiche,
                                                        Long atendenteId,
                                                        StatusSenha statusSenha) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (dataInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("inicio"), dataInicio.atStartOfDay()));
            }
            if (dataFim != null) {
                predicates.add(cb.lessThan(root.get("inicio"), dataFim.plusDays(1).atStartOfDay()));
            }
            if (guiche != null && !guiche.isBlank()) {
                predicates.add(cb.equal(root.get("guiche"), guiche.trim()));
            }
            if (atendenteId != null) {
                predicates.add(cb.equal(root.get("atendente").get("id"), atendenteId));
            }
            if (statusSenha != null) {
                Join<Object, Object> senha = root.join("senha");
                predicates.add(cb.equal(senha.get("status"), statusSenha));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
