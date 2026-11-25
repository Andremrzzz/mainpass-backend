package br.mainpass.acesso.convite;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Responde no mesmo formato do GlobalExceptionHandler do projeto ({status, codigo, mensagem}).
 * Precedencia maxima para que um eventual handler generico de Exception nao transforme isto em 500.
 */
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class ConviteExceptionHandler {

    public record Erro(int status, String codigo, String mensagem) {
    }

    @ExceptionHandler(ConviteException.class)
    public ResponseEntity<Erro> tratar(ConviteException e) {
        return ResponseEntity.status(e.getStatus())
                .body(new Erro(e.getStatus().value(), e.getCodigo(), e.getMessage()));
    }
}
