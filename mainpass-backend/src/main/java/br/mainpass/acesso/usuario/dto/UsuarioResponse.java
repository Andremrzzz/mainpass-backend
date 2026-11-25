package br.mainpass.acesso.usuario.dto;

import java.time.Instant;

import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;

/** Nunca expoe senha ou hash. */
public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        String cpf,
        String telefone,
        String bloco,
        String unidade,
        boolean ativo,
        Instant criadoEm) {

    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getNome(),
                u.getEmail(),
                u.getPerfil(),
                u.getCpf(),
                u.getTelefone(),
                u.getBloco(),
                u.getUnidade(),
                u.isAtivo(),
                u.getCriadoEm());
    }
}