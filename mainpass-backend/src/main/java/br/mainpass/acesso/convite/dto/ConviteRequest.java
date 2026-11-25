package br.mainpass.acesso.convite.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.Instant;

public record ConviteRequest(
        @NotNull @Valid ConvidadoDadosRequest convidado,
        @NotNull Long ambienteId,
        /** Opcional: padrao = agora. */
        Instant dataInicio,
        @NotNull Instant dataFim,
        /** Opcional: padrao = 1. Cada leitura aprovada na portaria consome um uso. */
        @Min(1) @Max(10) Integer usosMax,
        @Size(max = 255) String observacao) {
}
