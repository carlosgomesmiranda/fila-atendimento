package br.com.capacitabrasil.fila.exception;

public class TransicaoInvalidaException extends RegraNegocioException {

    public TransicaoInvalidaException(String mensagem) {
        super(mensagem, "TRANSICAO_INVALIDA");
    }
}
