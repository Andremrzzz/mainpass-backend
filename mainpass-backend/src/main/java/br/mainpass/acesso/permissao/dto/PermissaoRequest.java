package br.mainpass.acesso.permissao.dto;

import java.time.Instant;

import jakarta.validation.constraints.NotNull;

public record PermissaoRequest(
        @NotNull Long usuarioId,
        @NotNull Long ambienteId,
        Instant validoDe,
        Instant validoAte) {
}
