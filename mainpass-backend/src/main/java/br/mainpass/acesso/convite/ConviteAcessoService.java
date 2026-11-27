package br.mainpass.acesso.convite;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.ambiente.AmbienteRepository;
import br.mainpass.acesso.convite.ConviteValidacaoService.Motivo;
import br.mainpass.acesso.registro.MotivoRecusa;
import br.mainpass.acesso.registro.RegistroAcesso;
import br.mainpass.acesso.registro.RegistroAcessoRepository;
import br.mainpass.acesso.registro.ResultadoAcesso;
import br.mainpass.acesso.usuario.Usuario;
import jakarta.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Valida um QR Code de CONVITE na portaria E grava o RegistroAcesso, na mesma transacao:
 * o uso consumido, o status do convite e o registro (que alimenta GET /api/convidado/entradas)
 * ficam sempre consistentes.
 *
 * Os enums ResultadoAcesso e MotivoRecusa do projeto nao eram conhecidos ao escrever esta classe, entao as
 * constantes sao localizadas pelo NOME (palavras-chave nas tabelas abaixo). Na subida da aplicacao o mapa
 * resolvido e escrito no log; se algum motivo ficar sem correspondencia, o aviso diz qual. Edite as listas
 * de palavras (ou troque por constantes diretas) se os seus nomes forem outros.
 */
@Service
public class ConviteAcessoService {

    private static final Logger log = LoggerFactory.getLogger(ConviteAcessoService.class);

    /* ---- palavras-chave (ajuste aos nomes reais dos enums) ---- */
    private static final String[] RESULTADO_OK = {"AUTORIZ", "PERMIT", "LIBER"};
    private static final String[] RESULTADO_NEGADO = {"RECUS", "NEGAD", "BLOQ"};
    private static final String[] RESULTADO_INVALIDO = {"INVALID", "DESCONHEC", "NAO_ENCONTR"};

    private static final Map<Motivo, String[]> PALAVRAS_MOTIVO = new EnumMap<>(Map.of(
            Motivo.TOKEN_INVALIDO, new String[]{"INVALID", "NAO_ENCONTR", "DESCONHEC"},
            Motivo.PENDENTE_APROVACAO, new String[]{"PENDENT", "APROV", "NAO_AUTORIZ", "INVALID"},
            Motivo.RECUSADO, new String[]{"RECUS", "NEGAD", "NAO_AUTORIZ", "INVALID"},
            Motivo.CANCELADO, new String[]{"CANCEL", "REVOG", "INATIV", "INVALID"},
            Motivo.JA_UTILIZADO, new String[]{"UTILIZ", "USAD", "LIMITE", "INVALID"},
            Motivo.EXPIRADO, new String[]{"EXPIR", "VENC"},
            Motivo.AINDA_NAO_VALIDO, new String[]{"AINDA", "FORA", "HORARIO", "JANELA", "EXPIR"},
            Motivo.AMBIENTE_NAO_AUTORIZADO, new String[]{"AMBIENTE", "LOCAL", "PERMISS", "NAO_AUTORIZ"}));

    /** Resultado completo para o controller montar a resposta no formato que o front ja consome. */
    public record Resultado(boolean permitido, Motivo motivo, Usuario convidado, Convite convite, RegistroAcesso registro) {
    }

    private final ConviteValidacaoService validacao;
    private final RegistroAcessoRepository registros;
    private final AmbienteRepository ambientes;

    public ConviteAcessoService(ConviteValidacaoService validacao, RegistroAcessoRepository registros, AmbienteRepository ambientes) {
        this.validacao = validacao;
        this.registros = registros;
        this.ambientes = ambientes;
    }

    /** O token e de convite? O fluxo existente usa isto para decidir qual validador chamar. */
    public boolean ehTokenDeConvite(String token) {
        return validacao.ehTokenDeConvite(token);
    }

    @Transactional
    public Resultado validarERegistrar(String token, Long ambienteId, Usuario operador) {
        ConviteValidacaoService.Resultado r = validacao.validar(token, ambienteId);
        Ambiente ambiente = ambienteId == null ? null : ambientes.findById(ambienteId).orElse(null);

        RegistroAcesso registro = new RegistroAcesso();
        registro.setQrCode(null);                // convite nao usa a tabela qr_code
        registro.setUsuario(r.convidado());      // nulo quando o token e invalido
        registro.setAmbiente(ambiente);
        registro.setOperador(operador);
        registro.setResultado(resultado(r));
        registro.setMotivo(r.permitido() ? null : motivo(r.motivo()));
        registros.save(registro);

        return new Resultado(r.permitido(), r.motivo(), r.convidado(), r.convite(), registro);
    }

    /* ------------------------------ mapeamento ------------------------------ */

    private ResultadoAcesso resultado(ConviteValidacaoService.Resultado r) {
        if (r.permitido()) {
            return obrigatorio(EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_OK), "resultado autorizado");
        }
        Optional<ResultadoAcesso> especifico = r.motivo() == Motivo.TOKEN_INVALIDO
                ? EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_INVALIDO)
                : Optional.empty();
        return especifico.or(() -> EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_NEGADO))
                .or(() -> EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_INVALIDO))
                .orElseThrow(() -> new IllegalStateException(
                        "Nenhuma constante de ResultadoAcesso corresponde a uma recusa; ajuste RESULTADO_NEGADO"));
    }

    private MotivoRecusa motivo(Motivo motivo) {
        return EnumResolver.primeiro(MotivoRecusa.class, PALAVRAS_MOTIVO.get(motivo)).orElse(null); // coluna aceita nulo
    }

    private static <E> E obrigatorio(Optional<E> valor, String descricao) {
        return valor.orElseThrow(() -> new IllegalStateException(
                "Nenhuma constante de ResultadoAcesso corresponde a " + descricao + "; ajuste RESULTADO_OK"));
    }

    /** Mostra no log como cada situacao do convite foi gravada: confira na primeira subida. */
    @PostConstruct
    void registrarMapeamento() {
        log.info("Convites -> ResultadoAcesso: autorizado={}, recusado={}, token invalido={}",
                EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_OK).orElse(null),
                EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_NEGADO).orElse(null),
                EnumResolver.primeiro(ResultadoAcesso.class, RESULTADO_INVALIDO).orElse(null));
        for (Motivo m : Motivo.values()) {
            MotivoRecusa mr = motivo(m);
            if (mr == null) {
                log.warn("Convites: motivo {} sem constante correspondente em MotivoRecusa (sera gravado sem motivo). Ajuste PALAVRAS_MOTIVO.", m);
            } else {
                log.info("Convites -> MotivoRecusa: {} = {}", m, mr);
            }
        }
    }
}
