package br.mainpass.acesso.convite.dto;

import br.mainpass.acesso.convite.MensagemConvite;
import br.mainpass.acesso.convite.TipoMensagem;
import java.time.Instant;

/** `minha` indica se quem esta lendo foi o remetente; `lida` = o destinatario ja leu. */
public record MensagemResponse(
        Long id,
        Long remetenteId,
        String remetenteNome,
        boolean minha,
        TipoMensagem tipo,
        String conteudo,
        Instant criadoEm,
        boolean lida) {

    public static MensagemResponse from(MensagemConvite m, Long leitorId) {
        return new MensagemResponse(
                m.getId(),
                m.getRemetente().getId(),
                m.getRemetente().getNome(),
                m.getRemetente().getId().equals(leitorId),
                m.getTipo(),
                m.getConteudo(),
                m.getCriadoEm(),
                m.isLida());
    }
}
