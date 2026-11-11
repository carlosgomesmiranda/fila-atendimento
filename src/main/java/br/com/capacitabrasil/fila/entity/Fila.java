package br.com.capacitabrasil.fila.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "filas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Fila extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @Column(nullable = false, length = 80)
    private String nome;

    @Column(nullable = false, length = 2)
    private String prefixo;

    @Column(length = 255)
    private String descricao;

    @Column(nullable = false)
    private boolean ativa;

    @Column(name = "ultima_sequencia", nullable = false)
    private int ultimaSequencia;

    @Column(name = "data_ultima_sequencia")
    private LocalDate dataUltimaSequencia;

    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Calcula a proxima sequencia da fila. A numeracao reinicia a cada dia (RN001).
     */
    public int proximaSequencia(LocalDate dataReferencia) {
        if (dataReferencia.equals(this.dataUltimaSequencia)) {
            this.ultimaSequencia = this.ultimaSequencia + 1;
        } else {
            this.dataUltimaSequencia = dataReferencia;
            this.ultimaSequencia = 1;
        }
        return this.ultimaSequencia;
    }
}
