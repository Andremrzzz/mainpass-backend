package br.mainpass.acesso.ambiente.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AmbienteRequest(
        @NotBlank @Size(max = 120) String nome,
        @Size(max = 255) String descricao) {
}
