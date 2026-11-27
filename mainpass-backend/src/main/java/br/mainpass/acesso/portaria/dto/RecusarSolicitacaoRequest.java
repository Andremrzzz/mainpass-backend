package br.mainpass.acesso.portaria.dto;

import jakarta.validation.constraints.Size;

/** Corpo opcional de POST /api/portaria/convites/{id}/recusar. */
public record RecusarSolicitacaoRequest(@Size(max = 255) String motivo) {
}
