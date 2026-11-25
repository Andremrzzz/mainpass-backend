package br.mainpass.acesso.registro.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ValidarAcessoRequest(
        @NotBlank String token,
        @NotNull Long ambienteId) {
}
