package br.mainpass.acesso.convite.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Dados do convidado informados pelo morador. Se o e-mail ja pertencer a um CONVIDADO, o cadastro existente e reaproveitado. */
public record ConvidadoDadosRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @NotBlank @Pattern(regexp = "^(\\d{11}|\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2})$", message = "CPF invalido") String cpf,
        @Pattern(regexp = "^(\\d{10,11})?$", message = "Telefone deve ter 10 ou 11 digitos") String telefone) {
}
