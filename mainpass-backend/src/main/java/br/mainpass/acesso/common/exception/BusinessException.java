package br.mainpass.acesso.common.exception;

import org.springframework.http.HttpStatus;

/**
 * Violacao de regra de negocio. Carrega o status HTTP e um codigo estavel para o frontend.
 */
public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    public BusinessException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public static BusinessException badRequest(String codigo, String mensagem) {
        return new BusinessException(HttpStatus.BAD_REQUEST, codigo, mensagem);
    }

    public static BusinessException conflict(String codigo, String mensagem) {
        return new BusinessException(HttpStatus.CONFLICT, codigo, mensagem);
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCodigo() {
        return codigo;
    }
}
