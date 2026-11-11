package br.com.capacitabrasil.fila.repository.specification;

import br.com.capacitabrasil.fila.entity.Senha;
import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public final class SenhaSpecification {

    private SenhaSpecification() {
    }

    public static Specification<Senha> comFiltros(StatusSenha status, Long filaId, LocalDate dataReferencia) {
        return (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (status != null) {
                predicates.add(cb.equal(root.get("status"), status));
            }
            if (filaId != null) {
                predicates.add(cb.equal(root.get("fila").get("id"), filaId));
            }
            if (dataReferencia != null) {
                predicates.add(cb.equal(root.get("dataReferencia"), dataReferencia));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
    }
}
