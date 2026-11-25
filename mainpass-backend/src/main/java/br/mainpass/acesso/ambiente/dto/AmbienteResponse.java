package br.mainpass.acesso.ambiente.dto;

import java.time.Instant;

import br.mainpass.acesso.ambiente.Ambiente;

public record AmbienteResponse(
        Long id,
        String nome,
        String descricao,
        boolean ativo,
        Instant criadoEm) {

    public static AmbienteResponse from(Ambiente a) {
        return new AmbienteResponse(a.getId(), a.getNome(), a.getDescricao(), a.isAtivo(), a.getCriadoEm());
    }
}
