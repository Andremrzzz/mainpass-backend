package br.mainpass.acesso.registro.dto;

import java.time.Instant;

import br.mainpass.acesso.registro.MotivoRecusa;
import br.mainpass.acesso.registro.RegistroAcesso;
import br.mainpass.acesso.registro.ResultadoAcesso;

public record RegistroAcessoResponse(
        Long id,
        Long usuarioId,
        Long ambienteId,
        Long operadorId,
        ResultadoAcesso resultado,
        MotivoRecusa motivo,
        Instant dataHora) {

    public static RegistroAcessoResponse from(RegistroAcesso r) {
        return new RegistroAcessoResponse(
                r.getId(),
                r.getUsuario() != null ? r.getUsuario().getId() : null,
                r.getAmbiente() != null ? r.getAmbiente().getId() : null,
                r.getOperador().getId(),
                r.getResultado(),
                r.getMotivo(),
                r.getDataHora());
    }
}
