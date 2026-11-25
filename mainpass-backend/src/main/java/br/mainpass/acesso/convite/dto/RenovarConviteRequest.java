package br.mainpass.acesso.convite.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;

public record RenovarConviteRequest(
        @NotNull Instant dataFim,
        /** Opcional: usos extras concedidos junto com a nova validade (0-10). */
        @Min(0) @Max(10) Integer usosAdicionais) {
}
