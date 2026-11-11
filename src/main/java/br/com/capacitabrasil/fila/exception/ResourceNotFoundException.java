package br.com.capacitabrasil.fila.exception;

import lombok.Getter;

@Getter
public class ResourceNotFoundException extends RuntimeException {

    private final String codigo;

    public ResourceNotFoundException(String mensagem) {
        this(mensagem, "RECURSO_NAO_ENCONTRADO");
    }

    public ResourceNotFoundException(String mensagem, String codigo) {
        super(mensagem);
        this.codigo = codigo;
    }
}
