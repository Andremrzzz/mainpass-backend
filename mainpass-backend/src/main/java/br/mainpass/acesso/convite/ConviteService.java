package br.mainpass.acesso.convite;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.ambiente.AmbienteRepository;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.convite.dto.ConviteRequest;
import br.mainpass.acesso.convite.dto.ConviteResponse;
import br.mainpass.acesso.convite.dto.RenovarConviteRequest;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.time.Duration;
import java.time.Instant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ConviteService {

    static final Duration JANELA_MAXIMA = Duration.ofDays(30);
    static final Duration TOLERANCIA_INICIO = Duration.ofMinutes(5);
    static final int MAX_CONVITES_VIGENTES = 20;
    static final int MAX_SOLICITACOES_PENDENTES = 20;
    static final int TAMANHO_MAXIMO_PAGINA = 50;

    private final ConviteRepository convites;
    private final UsuarioRepository usuarios;
    private final AmbienteRepository ambientes;
    private final ConvidadoProvisioner provisioner;
    private final ConviteTokenService tokens;

    public ConviteService(ConviteRepository convites, UsuarioRepository usuarios, AmbienteRepository ambientes,
                          ConvidadoProvisioner provisioner, ConviteTokenService tokens) {
        this.convites = convites;
        this.usuarios = usuarios;
        this.ambientes = ambientes;
        this.provisioner = provisioner;
        this.tokens = tokens;
    }

    @Transactional
    public ConviteResponse criar(String login, ConviteRequest req) {
        Usuario morador = usuario(login);
        Ambiente ambiente = ambientes.findById(req.ambienteId())
                .orElseThrow(() -> new ResourceNotFoundException("Ambiente nao encontrado"));

        Instant agora = Instant.now();
        Instant inicio = req.dataInicio() == null ? agora : req.dataInicio();
        Instant fim = req.dataFim();

        if (inicio.isBefore(agora.minus(TOLERANCIA_INICIO))) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "INICIO_NO_PASSADO", "O inicio do convite nao pode estar no passado");
        }
        if (!fim.isAfter(inicio)) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "JANELA_INVALIDA", "O fim deve ser posterior ao inicio");
        }
        if (Duration.between(inicio, fim).compareTo(JANELA_MAXIMA) > 0) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "JANELA_LONGA", "A validade maxima de um convite e de 30 dias");
        }
        if (req.convidado().email().trim().equalsIgnoreCase(morador.getEmail())) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "AUTO_CONVITE", "Voce nao pode convidar a si mesmo");
        }
        if (convites.countByMoradorIdAndStatusAndDataFimAfter(morador.getId(), StatusConvite.ATIVO, agora) >= MAX_CONVITES_VIGENTES) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "LIMITE_DE_CONVITES", "Limite de convites ativos atingido");
        }

        if (convites.countByMoradorIdAndStatusAndDataFimAfter(morador.getId(), StatusConvite.PENDENTE_APROVACAO, agora) >= MAX_SOLICITACOES_PENDENTES) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "LIMITE_DE_SOLICITACOES", "Voce ja tem muitas solicitacoes aguardando a portaria");
        }

        var convidado = provisioner.obterOuCriar(req.convidado());
        String observacao = req.observacao() == null || req.observacao().isBlank() ? null : req.observacao().trim();
        int usosMax = req.usosMax() == null ? 1 : req.usosMax();

        // Nasce PENDENTE_APROVACAO: o QR Code so e liberado quando a portaria aprovar.
        Convite convite = convites.save(new Convite(morador, convidado.usuario(), ambiente, inicio, fim, usosMax, observacao));
        return paraResposta(convite, agora).comSenhaProvisoria(convidado.senhaProvisoria());
    }

    @Transactional(readOnly = true)
    public Page<ConviteResponse> listar(String login, StatusConvite status, int pagina, int tamanho) {
        Usuario morador = usuario(login);
        Instant agora = Instant.now();
        Pageable pageable = PageRequest.of(Math.max(pagina, 0), limitar(tamanho), Sort.by(Sort.Direction.DESC, "criadoEm"));

        Page<Convite> resultado;
        if (status == null) {
            resultado = convites.findByMoradorId(morador.getId(), pageable);
        } else if (status == StatusConvite.ATIVO) {
            resultado = convites.findVigentesDoMorador(morador.getId(), StatusConvite.ATIVO, agora, pageable);
        } else if (status == StatusConvite.EXPIRADO) {
            resultado = convites.findExpiradosDoMorador(morador.getId(), StatusConvite.ATIVO, StatusConvite.EXPIRADO, agora, pageable);
        } else {
            resultado = convites.findByMoradorIdAndStatus(morador.getId(), status, pageable);
        }
        return resultado.map(c -> paraResposta(c, agora));
    }

    @Transactional(readOnly = true)
    public ConviteResponse detalhar(String login, Long id) {
        return paraResposta(doMorador(login, id), Instant.now());
    }

    @Transactional
    public ConviteResponse cancelar(String login, Long id) {
        Convite convite = doMorador(login, id);
        Instant agora = Instant.now();
        StatusConvite atual = convite.statusEfetivo(agora);
        if (atual == StatusConvite.UTILIZADO || atual == StatusConvite.EXPIRADO || atual == StatusConvite.RECUSADO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "CONVITE_ENCERRADO", "Convites utilizados, expirados ou recusados nao podem ser cancelados");
        }
        convite.cancelar(agora); // idempotente para convites ja cancelados
        return paraResposta(convite, agora);
    }

    /** Concede mais tempo (resposta a uma solicitacao do convidado). A nova validade so pode avancar. */
    @Transactional
    public ConviteResponse renovar(String login, Long id, RenovarConviteRequest req) {
        Convite convite = doMorador(login, id);
        Instant agora = Instant.now();
        if (convite.getStatus() == StatusConvite.CANCELADO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "CONVITE_CANCELADO", "Convite cancelado nao pode ser renovado");
        }
        if (convite.getStatus() == StatusConvite.PENDENTE_APROVACAO || convite.getStatus() == StatusConvite.RECUSADO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "CONVITE_NAO_APROVADO",
                    "Somente convites aprovados pela portaria podem ser renovados");
        }
        Instant novoFim = req.dataFim();
        if (!novoFim.isAfter(agora) || !novoFim.isAfter(convite.getDataFim())) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "JANELA_INVALIDA", "A nova validade deve ser futura e posterior a atual");
        }
        if (Duration.between(convite.getDataInicio(), novoFim).compareTo(JANELA_MAXIMA) > 0) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "JANELA_LONGA", "A validade maxima de um convite e de 30 dias");
        }
        convite.estender(novoFim, req.usosAdicionais() == null ? 0 : req.usosAdicionais());
        return paraResposta(convite, agora);
    }

    /* ------------------------------ internos ------------------------------ */

    private ConviteResponse paraResposta(Convite c, Instant agora) {
        return ConviteResponse.from(c, agora, tokens.gerar(c.getId()));
    }

    /** Convite de outro morador e tratado como inexistente (nao revela que o id existe). */
    private Convite doMorador(String login, Long id) {
        Usuario morador = usuario(login);
        Convite convite = convites.findDetalhadoById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Convite nao encontrado"));
        if (!convite.getMorador().getId().equals(morador.getId())) {
            throw new ResourceNotFoundException("Convite nao encontrado");
        }
        return convite;
    }

    static int limitar(int tamanho) {
        return Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA);
    }

    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
