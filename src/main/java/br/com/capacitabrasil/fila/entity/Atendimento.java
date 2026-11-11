package br.com.capacitabrasil.fila.entity;

import br.com.capacitabrasil.fila.exception.TransicaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "atendimentos")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Atendimento extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "senha_id", nullable = false, unique = true)
    private Senha senha;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendente_id")
    private Usuario atendente;

    @Column
    private LocalDateTime inicio;

    @Column
    private LocalDateTime fim;

    @Column(length = 20)
    private String guiche;

    @Column(length = 500)
    private String observacao;

    /**
     * Marca o inicio efetivo do atendimento (a senha ja foi chamada).
     */
    public void iniciar(LocalDateTime agora, String guiche) {
        if (this.inicio != null) {
            throw new TransicaoInvalidaException(
                    "O atendimento da senha %s ja foi iniciado.".formatted(senha.getNumero()));
        }
        this.inicio = agora;
        if (guiche != null && !guiche.isBlank()) {
            this.guiche = guiche.trim();
        }
    }

    /**
     * Marca o fim do atendimento. O campo inicio e obrigatorio (RN005).
     */
    public void finalizar(LocalDateTime agora, String observacao) {
        if (this.inicio == null) {
            throw new TransicaoInvalidaException(
                    "O atendimento da senha %s ainda nao foi iniciado.".formatted(senha.getNumero()));
        }
        if (this.fim != null) {
            throw new TransicaoInvalidaException(
                    "O atendimento da senha %s ja foi finalizado.".formatted(senha.getNumero()));
        }
        this.fim = agora;
        if (observacao != null && !observacao.isBlank()) {
            this.observacao = observacao.trim();
        }
    }

    /**
     * Duracao efetiva do atendimento em segundos, quando iniciado e finalizado.
     */
    public Long duracaoSegundos() {
        if (inicio == null || fim == null) {
            return null;
        }
        return Duration.between(inicio, fim).toSeconds();
    }
}
