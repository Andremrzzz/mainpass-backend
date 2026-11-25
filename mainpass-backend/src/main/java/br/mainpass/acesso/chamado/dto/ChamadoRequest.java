package br.mainpass.acesso.chamado.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ChamadoRequest(
        @NotBlank(message = "Informe o assunto")
        @Size(min = 3, max = 150, message = "O assunto deve ter entre 3 e 150 caracteres")
        String assunto,

        @NotBlank(message = "Descreva o problema")
        @Size(min = 5, max = 2000, message = "A mensagem deve ter entre 5 e 2000 caracteres")
        String mensagem) {
}
