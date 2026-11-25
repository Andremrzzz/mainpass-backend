package br.mainpass.acesso.permissao.dto;

import java.time.Instant;

import br.mainpass.acesso.permissao.PermissaoAcesso;

public record PermissaoResponse(
        Long id,
        Long usuarioId,
        Long ambienteId,
        Instant validoDe,
        Instant validoAte) {

    public static PermissaoResponse from(PermissaoAcesso p) {
        return new PermissaoResponse(p.getId(), p.getUsuario().getId(), p.getAmbiente().getId(),
                p.getValidoDe(), p.getValidoAte());
    }
}
