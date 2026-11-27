package br.mainpass.acesso.convite;

import br.mainpass.acesso.usuario.Usuario;
import java.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Regra de validacao de QR Code de CONVITE na portaria. Chamado pelo fluxo existente de
 * POST /api/acessos/validar quando o token tiver o formato de convite (veja PATCHES.md).
 *
 * Em caso de sucesso consome um uso, na mesma transacao e com a linha travada, e devolve o
 * convidado para o fluxo existente gravar o RegistroAcesso em nome dele (e assim alimentar
 * GET /api/convidado/entradas). Recusas NAO lancam excecao: sao um resultado normal.
 */
@Service
public class ConviteValidacaoService {

    public enum Motivo {
        TOKEN_INVALIDO,
        PENDENTE_APROVACAO,
        RECUSADO,
        CANCELADO,
        JA_UTILIZADO,
        EXPIRADO,
        AINDA_NAO_VALIDO,
        AMBIENTE_NAO_AUTORIZADO
    }

    public record Resultado(boolean permitido, Motivo motivo, Convite convite, Usuario convidado) {
        static Resultado negado(Motivo motivo, Convite convite) {
            return new Resultado(false, motivo, convite, convite == null ? null : convite.getConvidado());
        }
    }

    private final ConviteRepository convites;
    private final ConviteTokenService tokens;

    public ConviteValidacaoService(ConviteRepository convites, ConviteTokenService tokens) {
        this.convites = convites;
        this.tokens = tokens;
    }

    /** O token e de convite? (permite ao fluxo existente decidir qual validador chamar). */
    public boolean ehTokenDeConvite(String token) {
        return token != null && token.trim().startsWith("MPC.");
    }

    @Transactional
    public Resultado validar(String token, Long ambienteId) {
        var id = tokens.verificar(token);
        if (id.isEmpty()) {
            return Resultado.negado(Motivo.TOKEN_INVALIDO, null);
        }
        Convite c = convites.findByIdForUpdate(id.get()).orElse(null);
        if (c == null) {
            return Resultado.negado(Motivo.TOKEN_INVALIDO, null);
        }

        Instant agora = Instant.now();
        // Convite ainda nao liberado pela portaria nunca passa (nem dentro do prazo)
        if (c.getStatus() == StatusConvite.PENDENTE_APROVACAO && c.statusEfetivo(agora) != StatusConvite.EXPIRADO) {
            return Resultado.negado(Motivo.PENDENTE_APROVACAO, c);
        }
        if (c.getStatus() == StatusConvite.RECUSADO) return Resultado.negado(Motivo.RECUSADO, c);
        if (c.getStatus() == StatusConvite.CANCELADO) return Resultado.negado(Motivo.CANCELADO, c);
        if (c.getStatus() == StatusConvite.UTILIZADO) return Resultado.negado(Motivo.JA_UTILIZADO, c);
        if (c.statusEfetivo(agora) == StatusConvite.EXPIRADO) return Resultado.negado(Motivo.EXPIRADO, c);
        if (agora.isBefore(c.getDataInicio())) return Resultado.negado(Motivo.AINDA_NAO_VALIDO, c);
        if (!c.getAmbiente().getId().equals(ambienteId)) return Resultado.negado(Motivo.AMBIENTE_NAO_AUTORIZADO, c);

        c.registrarUso();
        return new Resultado(true, null, c, c.getConvidado());
    }
}
