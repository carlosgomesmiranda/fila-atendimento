package br.com.capacitabrasil.fila.entity.enums;

/**
 * Status possiveis de uma senha.
 *
 * <pre>
 * AGUARDANDO -> CHAMADA -> EM_ATENDIMENTO -> ATENDIDA
 *      \            \
 *       \            +--> CANCELADA (nao compareceu)
 *        +---------------> CANCELADA
 * </pre>
 */
public enum StatusSenha {

    AGUARDANDO,
    CHAMADA,
    EM_ATENDIMENTO,
    ATENDIDA,
    CANCELADA;

    public boolean isTerminal() {
        return this == ATENDIDA || this == CANCELADA;
    }
}
