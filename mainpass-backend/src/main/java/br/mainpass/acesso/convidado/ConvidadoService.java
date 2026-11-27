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
import br.mainpass.acesso.registro.ResultadoAcesso;
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
    private static final String CAMPO_DATA = "dataHora";
    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final DateTimeFormatter FORMATO =
            DateTimeFormatter.ofPattern("dd/MM/yyyy 'as' HH:mm");

    private final UsuarioRepository usuarios;
    private final ConviteRepository convites;
    private final ConviteTokenService tokens;
    private final MensagemConviteService chat;
    private final RegistroAcessoRepository registros;

    public ConvidadoService(
            UsuarioRepository usuarios,
            ConviteRepository convites,
            ConviteTokenService tokens,
            MensagemConviteService chat,
            RegistroAcessoRepository registros
    ) {
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

        List<Convite> lista = convites.findDoConvidado(
                u.getId(), PageRequest.ofSize(MAX_CONVITES)
        );

        Convite principal = lista.stream()
                .filter(c -> c.statusEfetivo(agora) == StatusConvite.ATIVO)
                .min(Comparator.comparing(Convite::getDataFim))
                .orElse(lista.isEmpty() ? null : lista.get(0));

        ConvidadoPerfilResponse.Anfitriao anfitriao = null;
        ConvidadoPerfilResponse.ConvitePrincipal convite = null;

        if (principal != null) {
            Usuario morador = principal.getMorador();

            anfitriao = new ConvidadoPerfilResponse.Anfitriao(
                    morador.getId(),
                    morador.getNome(),
                    morador.getBloco(),
                    morador.getUnidade(),
                    morador.getTelefone()
            );

            convite = new ConvidadoPerfilResponse.ConvitePrincipal(
                    principal.getId(),
                    principal.getDataInicio(),
                    principal.getDataFim(),
                    principal.getObservacao()
            );
        }

        return new ConvidadoPerfilResponse(
                u.getNome(),
                u.getEmail(),
                u.getTelefone(),
                "CPF",
                mascararCpf(u.getCpf()),
                anfitriao,
                convite
        );
    }

    @Transactional(readOnly = true)
    public List<ConvidadoQrCodeResponse> qrcodes(String login) {
        Usuario u = usuario(login);
        Instant agora = Instant.now();

        return convites.findDoConvidado(u.getId(), PageRequest.ofSize(MAX_CONVITES))
                .stream()
                .map(c -> ConvidadoQrCodeResponse.from(c, agora, tokens.gerar(c.getId())))
                .toList();
    }

    @Transactional(readOnly = true)
    public Page<EntradaResponse> entradas(String login, int pagina, int tamanho) {
        Usuario convidado = usuario(login);

        PageRequest pageable = PageRequest.of(
                Math.max(pagina, 0),
                Math.min(Math.max(tamanho, 1), MAX_POR_PAGINA),
                Sort.by(Sort.Direction.DESC, CAMPO_DATA, "id")
        );

        return registros.buscarEntradasDoConvidado(convidado.getId(), pageable)
                .map(this::paraEntrada);
    }

    @Transactional
    public MensagemResponse solicitarRenovacao(
            String login, Long conviteId, SolicitarRenovacaoRequest req
    ) {
        StringBuilder texto = new StringBuilder("Solicitacao de renovacao do convite");

        if (req != null && req.novaDataFim() != null) {
            texto.append(": gostaria de acesso ate ")
                    .append(FORMATO.format(req.novaDataFim().atZone(FUSO)));
        }

        if (req != null && req.motivo() != null && !req.motivo().isBlank()) {
            texto.append(". Motivo: ").append(req.motivo().trim());
        }

        return chat.solicitarRenovacao(login, conviteId, texto.toString());
    }

    private EntradaResponse paraEntrada(RegistroAcesso registro) {
        boolean permitido = registro.getResultado() == ResultadoAcesso.AUTORIZADO;
        Ambiente ambiente = registro.getAmbiente();

        String motivo = null;
        if (!permitido && registro.getMotivo() != null) {
            motivo = registro.getMotivo().name();
        }

        return new EntradaResponse(
                registro.getId(),
                registro.getDataHora(),
                ambiente == null ? null : ambiente.getNome(),
                "ENTRADA",
                "QR Code Convidado",
                permitido ? "PERMITIDO" : "NEGADO",
                motivo
        );
    }

    static String mascararCpf(String cpf) {
        String d = cpf == null ? "" : cpf.replaceAll("\\D", "");
        return d.length() == 11
                ? "***." + d.substring(3, 6) + "." + d.substring(6, 9) + "-**"
                : "***";
    }

    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}