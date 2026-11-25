package br.mainpass.acesso.convidado.dto;

import java.time.Instant;

/** resultado: AUTORIZADO | NEGADO. motivoRecusa: nome do enum de recusa (null quando autorizado). */
public record EntradaResponse(
        Long id,
        Instant dataHora,
        Long ambienteId,
        String ambienteNome,
        String resultado,
        String motivoRecusa) {
}
