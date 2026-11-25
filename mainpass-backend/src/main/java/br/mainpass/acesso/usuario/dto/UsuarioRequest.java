package br.mainpass.acesso.usuario.dto;

import br.mainpass.acesso.usuario.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UsuarioRequest(
        @NotBlank @Size(max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        @Size(min = 8, max = 72) String senha,
        @NotNull Perfil perfil,
        @Size(max = 14) String cpf,
        @Size(max = 20) String telefone,
        @Size(max = 20) String bloco,
        @Size(max = 20) String unidade) {
}