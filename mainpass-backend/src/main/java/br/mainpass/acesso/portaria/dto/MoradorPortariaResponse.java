package br.mainpass.acesso.portaria.dto;

import br.mainpass.acesso.usuario.Usuario;

/**
 * Morador como a portaria o ve. Nunca inclui senha/hash, perfil interno ou tokens de QR Code.
 * `convitesVigentes` = convidados com convite valido agora (so na listagem; no detalhe vem a lista completa).
 */
public record MoradorPortariaResponse(
        Long id,
        String nome,
        String cpf,
        String email,
        String telefone,
        String bloco,
        String unidade,
        boolean ativo,
        long convitesVigentes) {

    public static MoradorPortariaResponse from(Usuario u, long convitesVigentes) {
        return new MoradorPortariaResponse(u.getId(), u.getNome(), u.getCpf(), u.getEmail(), u.getTelefone(),
                u.getBloco(), u.getUnidade(), u.isAtivo(), convitesVigentes);
    }
}
