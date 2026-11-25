package br.mainpass.acesso.convite.dto;

import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.StatusConvite;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/**
 * Visao do convite para o MORADOR.
 * {@code token} so vem preenchido enquanto o convite estiver ATIVO; {@code senhaProvisoria} so na criacao,
 * quando um novo usuario CONVIDADO foi cadastrado (entregue uma unica vez).
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConviteResponse(
        Long id,
        Long convidadoId,
        String convidadoNome,
        String convidadoEmail,
        String convidadoTelefone,
        Long ambienteId,
        String ambienteNome,
        Instant dataInicio,
        Instant dataFim,
        StatusConvite status,
        int usos,
        int usosMax,
        String observacao,
        Instant criadoEm,
        String token,
        String senhaProvisoria) {

    public static ConviteResponse from(Convite c, Instant agora, String token) {
        StatusConvite status = c.statusEfetivo(agora);
        return new ConviteResponse(
                c.getId(),
                c.getConvidado().getId(),
                c.getConvidado().getNome(),
                c.getConvidado().getEmail(),
                c.getConvidado().getTelefone(),
                c.getAmbiente().getId(),
                c.getAmbiente().getNome(), // ASSUMIDO: Ambiente#getNome()
                c.getDataInicio(),
                c.getDataFim(),
                status,
                c.getUsos(),
                c.getUsosMax(),
                c.getObservacao(),
                c.getCriadoEm(),
                status == StatusConvite.ATIVO ? token : null,
                null);
    }

    public ConviteResponse comSenhaProvisoria(String senha) {
        return new ConviteResponse(id, convidadoId, convidadoNome, convidadoEmail, convidadoTelefone, ambienteId,
                ambienteNome, dataInicio, dataFim, status, usos, usosMax, observacao, criadoEm, token, senha);
    }
}
