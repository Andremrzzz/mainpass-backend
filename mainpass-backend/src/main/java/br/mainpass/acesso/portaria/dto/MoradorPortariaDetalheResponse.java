package br.mainpass.acesso.portaria.dto;

import java.util.List;

public record MoradorPortariaDetalheResponse(MoradorPortariaResponse morador, List<ConviteDoMoradorResponse> convidados) {
}
