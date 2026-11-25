package br.mainpass.acesso.convite;

import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.convite.dto.MensagemResponse;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Chat de um convite: so o morador anfitriao e o convidado daquele convite participam (outros: 404). */
@Service
public class MensagemConviteService {

    static final int LIMITE_POR_LEITURA = 200;

    private final MensagemConviteRepository mensagens;
    private final ConviteRepository convites;
    private final UsuarioRepository usuarios;

    public MensagemConviteService(MensagemConviteRepository mensagens, ConviteRepository convites, UsuarioRepository usuarios) {
        this.mensagens = mensagens;
        this.convites = convites;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<MensagemResponse> listar(String login, Long conviteId, Long apos) {
        Usuario leitor = usuario(login);
        conviteDoParticipante(conviteId, leitor);
        return mensagens
                .findByConviteIdAndIdGreaterThanOrderByIdAsc(conviteId, apos == null ? 0L : apos, PageRequest.ofSize(LIMITE_POR_LEITURA))
                .stream().map(m -> MensagemResponse.from(m, leitor.getId())).toList();
    }

    @Transactional
    public MensagemResponse enviar(String login, Long conviteId, String conteudo) {
        Usuario remetente = usuario(login);
        Convite convite = conviteDoParticipante(conviteId, remetente);
        return gravar(convite, remetente, TipoMensagem.TEXTO, conteudo);
    }

    @Transactional
    public void marcarComoLidas(String login, Long conviteId) {
        Usuario leitor = usuario(login);
        conviteDoParticipante(conviteId, leitor);
        mensagens.marcarComoLidas(conviteId, leitor.getId());
    }

    /** Solicitacao de renovacao: so o CONVIDADO do convite pode abri-la. */
    @Transactional
    public MensagemResponse solicitarRenovacao(String login, Long conviteId, String conteudo) {
        Usuario remetente = usuario(login);
        Convite convite = conviteDoParticipante(conviteId, remetente);
        if (!convite.getConvidado().getId().equals(remetente.getId())) {
            throw new ResourceNotFoundException("Convite nao encontrado");
        }
        return gravar(convite, remetente, TipoMensagem.SOLICITACAO_RENOVACAO, conteudo);
    }

    /* ------------------------------ internos ------------------------------ */

    private MensagemResponse gravar(Convite convite, Usuario remetente, TipoMensagem tipo, String conteudo) {
        if (convite.statusEfetivo(Instant.now()) == StatusConvite.CANCELADO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "CONVITE_CANCELADO", "Convite cancelado: o chat esta encerrado");
        }
        String texto = conteudo.trim();
        if (texto.isEmpty()) {
            throw ConviteException.regra(HttpStatus.BAD_REQUEST, "MENSAGEM_VAZIA", "Digite uma mensagem");
        }
        MensagemConvite salva = mensagens.save(new MensagemConvite(convite, remetente, tipo, texto));
        return MensagemResponse.from(salva, remetente.getId());
    }

    private Convite conviteDoParticipante(Long conviteId, Usuario usuario) {
        Convite convite = convites.findById(conviteId)
                .orElseThrow(() -> new ResourceNotFoundException("Convite nao encontrado"));
        boolean participante = convite.getMorador().getId().equals(usuario.getId())
                || convite.getConvidado().getId().equals(usuario.getId());
        if (!participante) {
            throw new ResourceNotFoundException("Convite nao encontrado");
        }
        return convite;
    }

    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
