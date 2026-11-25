package br.mainpass.acesso.registro.dto;

import br.mainpass.acesso.registro.MotivoRecusa;
import br.mainpass.acesso.registro.ResultadoAcesso;

/** Resposta para o leitor/operador. nomeUsuario so vem preenchido quando o acesso e autorizado. */
public record ValidarAcessoResponse(
        ResultadoAcesso resultado,
        MotivoRecusa motivo,
        String nomeUsuario) {
}
