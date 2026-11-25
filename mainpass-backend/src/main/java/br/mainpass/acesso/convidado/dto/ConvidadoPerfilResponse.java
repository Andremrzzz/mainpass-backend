package br.mainpass.acesso.convidado.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

/** Perfil do convidado + anfitriao e convite "principal" (vigente mais proximo de vencer, senao o mais recente). */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ConvidadoPerfilResponse(
        String nome,
        String email,
        String telefone,
        String tipoDocumento,
        /** CPF ja mascarado (***.456.789-**): o back-end nunca devolve o documento inteiro. */
        String documento,
        Anfitriao anfitriao,
        ConvitePrincipal convite) {

    public record Anfitriao(Long id, String nome, String bloco, String unidade, String telefone) {
    }

    public record ConvitePrincipal(Long conviteId, Instant inicio, Instant fim, String observacao) {
    }
}
