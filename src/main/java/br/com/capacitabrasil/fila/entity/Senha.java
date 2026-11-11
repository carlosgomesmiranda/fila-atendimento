package br.com.capacitabrasil.fila.entity;

import br.com.capacitabrasil.fila.entity.enums.StatusSenha;
import br.com.capacitabrasil.fila.exception.TransicaoInvalidaException;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "senhas")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode(onlyExplicitlyIncluded = true, callSuper = false)
public class Senha extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @EqualsAndHashCode.Include
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "fila_id", nullable = false)
    private Fila fila;

    @Column(nullable = false, length = 8)
    private String numero;

    @Column(nullable = false)
    private int sequencia;

    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusSenha status;

    @Column(name = "data_geracao", nullable = false)
    private LocalDateTime dataGeracao;

    @Column(name = "hora_chamada")
    private LocalDateTime horaChamada;

    @Column(name = "hora_inicio_atendimento")
    private LocalDateTime horaInicioAtendimento;

    @Column(name = "hora_fim_atendimento")
    private LocalDateTime horaFimAtendimento;

    @Version
    @Column(nullable = false)
    private Long version;

    /**
     * Transicao AGUARDANDO -> CHAMADA.
     */
    public void chamar(LocalDateTime agora) {
        exigirStatus(StatusSenha.AGUARDANDO, "chamar");
        this.status = StatusSenha.CHAMADA;
        this.horaChamada = agora;
    }

    /**
     * Transicao CHAMADA -> EM_ATENDIMENTO.
     */
    public void iniciarAtendimento(LocalDateTime agora) {
        exigirStatus(StatusSenha.CHAMADA, "iniciar o atendimento de");
        this.status = StatusSenha.EM_ATENDIMENTO;
        this.horaInicioAtendimento = agora;
    }

    /**
     * Transicao EM_ATENDIMENTO -> ATENDIDA (RN005).
     */
    public void finalizarAtendimento(LocalDateTime agora) {
        exigirStatus(StatusSenha.EM_ATENDIMENTO, "finalizar o atendimento de");
        this.status = StatusSenha.ATENDIDA;
        this.horaFimAtendimento = agora;
    }

    /**
     * Cancelamento definitivo: a senha nao retorna a fila (RN006).
     */
    public void cancelar() {
        if (status.isTerminal()) {
            throw new TransicaoInvalidaException(
                    "A senha %s ja esta %s e nao pode ser cancelada.".formatted(numero, status));
        }
        this.status = StatusSenha.CANCELADA;
    }

    public boolean isAguardando() {
        return status == StatusSenha.AGUARDANDO;
    }

    private void exigirStatus(StatusSenha esperado, String acao) {
        if (this.status != esperado) {
            throw new TransicaoInvalidaException(
                    "Nao e possivel %s a senha %s: status atual %s, status esperado %s."
                            .formatted(acao, numero, status, esperado));
        }
    }
}
