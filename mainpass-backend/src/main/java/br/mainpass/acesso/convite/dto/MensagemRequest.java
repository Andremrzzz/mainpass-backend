package br.mainpass.acesso.convite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MensagemRequest(@NotBlank @Size(max = 500) String conteudo) {
}
