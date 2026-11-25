package br.mainpass.acesso.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String recurso, Long id) {
        super(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", recurso + " nao encontrado(a): id " + id);
    }

    public ResourceNotFoundException(String mensagem) {
        super(HttpStatus.NOT_FOUND, "NAO_ENCONTRADO", mensagem);
    }
}
