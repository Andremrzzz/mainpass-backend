package br.mainpass.acesso.convidado.dto;

import jakarta.validation.constraints.Size;
import java.time.Instant;

/** Ambos opcionais: o convidado pode apenas pedir "mais tempo" ou sugerir ate quando. */
public record SolicitarRenovacaoRequest(
        @Size(max = 300) String motivo,
        Instant novaDataFim) {
}
