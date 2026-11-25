package br.mainpass.acesso.common.exception;

import java.time.Instant;
import java.util.List;

/**
 * Formato unico de erro da API. O frontend pode confiar sempre nesta estrutura.
 */
public record ErrorResponse(
        Instant timestamp,
        int status,
        String codigo,
        String mensagem,
        List<CampoInvalido> campos) {

    public record CampoInvalido(String campo, String mensagem) {
    }

    public static ErrorResponse of(int status, String codigo, String mensagem) {
        return new ErrorResponse(Instant.now(), status, codigo, mensagem, List.of());
    }

    public static ErrorResponse of(int status, String codigo, String mensagem, List<CampoInvalido> campos) {
        return new ErrorResponse(Instant.now(), status, codigo, mensagem, campos);
    }
}
