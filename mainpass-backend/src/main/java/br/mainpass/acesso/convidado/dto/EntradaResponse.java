package br.mainpass.acesso.convidado.dto;

import java.time.Instant;

public record EntradaResponse(
        Long id,
        Instant accessedAt,
        String location,
        String direction,
        String passType,
        String status,
        String denialReason
) {}