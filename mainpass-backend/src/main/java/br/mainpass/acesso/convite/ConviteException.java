package br.mainpass.acesso.convite;

import org.springframework.http.HttpStatus;

/** Violacao de regra de negocio do modulo de convites (400/403/409). Tratada por {@link ConviteExceptionHandler}. */
public class ConviteException extends RuntimeException {

    private final HttpStatus status;
    private final String codigo;

    private ConviteException(HttpStatus status, String codigo, String mensagem) {
        super(mensagem);
        this.status = status;
        this.codigo = codigo;
    }

    public static ConviteException regra(HttpStatus status, String codigo, String mensagem) {
        return new ConviteException(status, codigo, mensagem);
    }

    public HttpStatus getStatus() { return status; }
    public String getCodigo() { return codigo; }
}
