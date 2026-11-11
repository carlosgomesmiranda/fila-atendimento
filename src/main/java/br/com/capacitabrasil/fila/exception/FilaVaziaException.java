package br.com.capacitabrasil.fila.exception;

public class FilaVaziaException extends RegraNegocioException {

    public FilaVaziaException(String nomeFila) {
        super("Nao ha senhas aguardando na fila %s.".formatted(nomeFila), "FILA_VAZIA");
    }
}
