package br.mainpass.acesso.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Cadastro publico (auto-cadastro). NAO tem campo de perfil de proposito: qualquer "perfil"/"role" enviado
 * no JSON e ignorado, e a conta sempre nasce CONVIDADO (veja {@link CadastroService}).
 * A conta de Morador (USUARIO) so e criada pela portaria, autenticada.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CadastroRequest(
        @NotBlank @Size(min = 3, max = 120) String nome,
        @NotBlank @Email @Size(max = 160) String email,
        // mesmas regras da tela: 8+ caracteres, maiuscula, minuscula e numero (max. 72 = limite do BCrypt)
        @NotBlank @Size(min = 8, max = 72)
        @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).+$",
                message = "A senha deve ter letra maiuscula, minuscula e numero")
        String senha) {
}
