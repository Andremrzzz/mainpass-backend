package br.mainpass.acesso.portaria.dto;

import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.StatusConvite;
import java.time.Instant;

/**
 * Solicitacao de convite vista pela portaria (fila de aprovacao). Nunca carrega o token do QR Code.
 * O CPF do convidado vai completo: a tela decide como exibir (mascara por padrao).
 */
public record SolicitacaoConviteResponse(
        Long conviteId,
        StatusConvite status,
        Morador morador,
        Convidado convidado,
        Long ambienteId,
        String ambienteNome,
        Instant dataInicio,
        Instant dataFim,
        int usosMax,
        String observacao,
        Instant criadoEm,
        Instant decididoEm,
        String motivoRecusa) {

    public record Morador(Long id, String nome, String bloco, String unidade, String telefone) {
    }

    public record Convidado(Long id, String nome, String cpf, String email, String telefone) {
    }

    public static SolicitacaoConviteResponse from(Convite c, Instant agora) {
        var m = c.getMorador();
        var g = c.getConvidado();
        return new SolicitacaoConviteResponse(
                c.getId(),
                c.statusEfetivo(agora),
                new Morador(m.getId(), m.getNome(), m.getBloco(), m.getUnidade(), m.getTelefone()),
                new Convidado(g.getId(), g.getNome(), g.getCpf(), g.getEmail(), g.getTelefone()),
                c.getAmbiente().getId(),
                c.getAmbiente().getNome(), // ASSUMIDO: Ambiente#getNome()
                c.getDataInicio(),
                c.getDataFim(),
                c.getUsosMax(),
                c.getObservacao(),
                c.getCriadoEm(),
                c.getDecididoEm(),
                c.getMotivoRecusa());
    }
}
