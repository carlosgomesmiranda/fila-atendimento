package br.com.capacitabrasil.fila.exception;

import lombok.Getter;

@Getter
public class RegraNegocioException extends RuntimeException {

    private final String codigo;

    public RegraNegocioException(String mensagem, String codigo) {
        super(mensagem);
        this.codigo = codigo;
    }
}
