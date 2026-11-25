package br.mainpass.acesso.chamado.dto;

import br.mainpass.acesso.chamado.Chamado;
import br.mainpass.acesso.chamado.StatusChamado;
import java.time.Instant;

public record ChamadoResponse(
        Long id,
        String assunto,
        String mensagem,
        StatusChamado status,
        Instant criadoEm,
        Instant resolvidoEm,
        Long solicitanteId,
        String solicitanteNome,
        String solicitanteBloco,
        String solicitanteUnidade) {

    /** Chamar dentro da transacao (solicitante e LAZY). */
    public static ChamadoResponse from(Chamado c) {
        return new ChamadoResponse(c.getId(), c.getAssunto(), c.getMensagem(), c.getStatus(),
                c.getCriadoEm(), c.getResolvidoEm(),
                c.getSolicitante().getId(), c.getSolicitante().getNome(),
                c.getSolicitante().getBloco(), c.getSolicitante().getUnidade());
    }
}
