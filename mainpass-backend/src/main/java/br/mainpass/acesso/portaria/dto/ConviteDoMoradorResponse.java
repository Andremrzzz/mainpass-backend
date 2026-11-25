package br.mainpass.acesso.portaria.dto;

import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.StatusConvite;
import java.time.Instant;

/** Convidado de um morador (um convite = um convidado + um ambiente + uma janela). Sem o token do QR Code. */
public record ConviteDoMoradorResponse(
        Long conviteId,
        Long convidadoId,
        String convidadoNome,
        String convidadoCpf,
        String convidadoTelefone,
        Long ambienteId,
        String ambienteNome,
        Instant dataInicio,
        Instant dataFim,
        StatusConvite status,
        int usos,
        int usosMax,
        String observacao) {

    public static ConviteDoMoradorResponse from(Convite c, Instant agora) {
        return new ConviteDoMoradorResponse(
                c.getId(),
                c.getConvidado().getId(),
                c.getConvidado().getNome(),
                c.getConvidado().getCpf(),
                c.getConvidado().getTelefone(),
                c.getAmbiente().getId(),
                c.getAmbiente().getNome(), // ASSUMIDO: Ambiente#getNome()
                c.getDataInicio(),
                c.getDataFim(),
                c.statusEfetivo(agora),
                c.getUsos(),
                c.getUsosMax(),
                c.getObservacao());
    }
}
