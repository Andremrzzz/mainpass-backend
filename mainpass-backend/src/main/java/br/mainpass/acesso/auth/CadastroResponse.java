package br.mainpass.acesso.auth;

import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;

/** Resposta do cadastro: nunca devolve a senha nem o hash. */
public record CadastroResponse(Long id, String nome, String email, Perfil perfil) {

    public static CadastroResponse from(Usuario u) {
        return new CadastroResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil());
    }
}
