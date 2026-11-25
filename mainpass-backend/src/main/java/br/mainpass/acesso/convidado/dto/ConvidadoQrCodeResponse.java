package br.mainpass.acesso.convidado.dto;

import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.StatusConvite;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * QR Code (convite) do ponto de vista do CONVIDADO. `token` so vem enquanto ATIVO; o front desenha o QR com ele.
 * `conviteId` identifica tambem a conversa (/api/convites/{conviteId}/mensagens).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConvidadoQrCodeResponse(
        Long conviteId,
        String token,
        Long ambienteId,
        String ambienteNome,
        String anfitriaoNome,
        Instant dataInicio,
        Instant dataFim,
        int usos,
        int usosMax,
        StatusConvite status) {

    public static ConvidadoQrCodeResponse from(Convite c, Instant agora, String token) {
        StatusConvite status = c.statusEfetivo(agora);
        return new ConvidadoQrCodeResponse(
                c.getId(),
                status == StatusConvite.ATIVO ? token : null,
                c.getAmbiente().getId(),
                c.getAmbiente().getNome(), // ASSUMIDO: Ambiente#getNome()
                c.getMorador().getNome(),
                c.getDataInicio(),
                c.getDataFim(),
                c.getUsos(),
                c.getUsosMax(),
                status);
    }
}
