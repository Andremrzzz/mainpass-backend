package br.mainpass.acesso.convidado;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.convidado.dto.ConvidadoPerfilResponse;
import br.mainpass.acesso.convidado.dto.ConvidadoQrCodeResponse;
import br.mainpass.acesso.convidado.dto.EntradaResponse;
import br.mainpass.acesso.convidado.dto.SolicitarRenovacaoRequest;
import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.ConviteRepository;
import br.mainpass.acesso.convite.ConviteTokenService;
import br.mainpass.acesso.convite.MensagemConviteService;
import br.mainpass.acesso.convite.StatusConvite;
import br.mainpass.acesso.convite.dto.MensagemResponse;
import br.mainpass.acesso.registro.RegistroAcesso;
import br.mainpass.acesso.registro.RegistroAcessoRepository;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConvidadoService {

    private static final int MAX_CONVITES = 100;
    private static final int MAX_POR_PAGINA = 50;
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    /** Atributo de data/hora de RegistroAcesso (usado na ordenacao). */
    private static final String CAMPO_DATA = "dataHora";

    private final UsuarioRepository usuarios;
    private final ConviteRepository convites;
    private final ConviteTokenService tokens;
    private final MensagemConviteService chat;
    private final RegistroAcessoRepository registros;

    public ConvidadoService(UsuarioRepository usuarios, ConviteRepository convites, ConviteTokenService tokens,
                            MensagemConviteService chat, RegistroAcessoRepository registros) {
        this.usuarios = usuarios;
        this.convites = convites;
        this.tokens = tokens;
        this.chat = chat;
        this.registros = registros;
    }

    @Transactional(readOnly = true)
    public ConvidadoPerfilResponse perfil(String login) {
        Usuario u = usuario(login);
        Instant agora = Instant.now();
        List<Convite> lista = convites.findDoConvidado(u.getId(), PageRequest.ofSize(MAX_CONVITES));

        Convite principal = lista.stream()
                .filter(c -> c.statusEfetivo(agora) == StatusConvite.ATIVO)
                .min(Comparator.comparing(Convite::getDataFim))
                .orElse(lista.isEmpty() ? null : lista.get(0)); // lista ja vem do mais recente para o mais antigo

        ConvidadoPerfilResponse.Anfitriao anfitriao = null;
        ConvidadoPerfilResponse.ConvitePrincipal convite = null;
        if (principal != null) {
            Usuario m = principal.getMorador();
            anfitriao = new ConvidadoPerfilResponse.Anfitriao(m.getId(), m.getNome(), m.getBloco(), m.getUnidade(), m.getTelefone());
            convite = new ConvidadoPerfilResponse.ConvitePrincipal(principal.getId(), principal.getDataInicio(),
                    principal.getDataFim(), principal.getObservacao());
        }
        return new ConvidadoPerfilResponse(u.getNome(), u.getEmail(), u.getTelefone(), "CPF", mascararCpf(u.getCpf()), anfitriao, convite);
    }

    @Transactional(readOnly = true)
    public List<ConvidadoQrCodeResponse> qrcodes(String login) {
        Usuario u = usuario(login);
        Instant agora = Instant.now();
        return convites.findDoConvidado(u.getId(), PageRequest.ofSize(MAX_CONVITES)).stream()
                .map(c -> ConvidadoQrCodeResponse.from(c, agora, tokens.gerar(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EntradaResponse> entradas(String login, int pagina, int tamanho) {
        Usuario u = usuario(login);
        var pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), MAX_POR_PAGINA),
                Sort.by(Sort.Direction.DESC, CAMPO_DATA));
        // exige RegistroAcessoRepository#findByUsuarioId(Long, Pageable) (veja PATCHES.md)
        return registros.findByUsuarioId(u.getId(), pageable).map(this::paraEntrada);
    }

    @Transactional
    public MensagemResponse solicitarRenovacao(String login, Long conviteId, SolicitarRenovacaoRequest req) {
        StringBuilder texto = new StringBuilder("Solicitacao de renovacao do convite");
        if (req != null && req.novaDataFim() != null) {
            texto.append(": gostaria de acesso ate ").append(FORMATO.format(req.novaDataFim().atZone(FUSO)));
        }
        if (req != null && req.motivo() != null && !req.motivo().isBlank()) {
            texto.append(". Motivo: ").append(req.motivo().trim());
        }
        return chat.solicitarRenovacao(login, conviteId, texto.toString());
    }

    /* ------------------------------ internos ------------------------------ */

    private EntradaResponse paraEntrada(RegistroAcesso r) {
        // ASSUMIDO: nomes dos valores de ResultadoAcesso (aceita AUTORIZADO*/PERMITIDO*; o resto vira NEGADO)
        String resultado = r.getResultado().name();
        boolean autorizado = resultado.startsWith("AUTORIZ") || resultado.startsWith("PERMIT");
        Ambiente ambiente = r.getAmbiente(); // pode ser nulo (codigo lido sem ambiente)
        return new EntradaResponse(
                r.getId(),
                r.getDataHora(),
                ambiente == null ? null : ambiente.getId(),
                ambiente == null ? null : ambiente.getNome(),
                autorizado ? "AUTORIZADO" : "NEGADO",
                r.getMotivo() == null ? null : r.getMotivo().name());
    }

    // 123.456.789-09 (ou 12345678909) -> ***.456.789-**
    static String mascararCpf(String cpf) {
        String d = cpf == null ? "" : cpf.replaceAll("\\D", "");
        return d.length() == 11 ? "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**" : "***";
    }

    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
