package br.mainpass.acesso.portaria;

import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.convite.Convite;
import br.mainpass.acesso.convite.ConviteException;
import br.mainpass.acesso.convite.ConviteRepository;
import br.mainpass.acesso.convite.StatusConvite;
import br.mainpass.acesso.portaria.dto.SolicitacaoConviteResponse;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Fila de aprovacao de convites da portaria: listar pendentes, aprovar e recusar. */
@Service
public class PortariaSolicitacaoService {

    static final int MAX_FILA = 200;

    private final ConviteRepository convites;
    private final UsuarioRepository usuarios;

    public PortariaSolicitacaoService(ConviteRepository convites, UsuarioRepository usuarios) {
        this.convites = convites;
        this.usuarios = usuarios;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoConviteResponse> pendentes() {
        Instant agora = Instant.now();
        return convites.findPendentesDeAprovacao(StatusConvite.PENDENTE_APROVACAO, agora, PageRequest.ofSize(MAX_FILA))
                .stream().map(c -> SolicitacaoConviteResponse.from(c, agora)).toList();
    }

    @Transactional
    public SolicitacaoConviteResponse aprovar(String loginOperador, Long id) {
        Usuario operador = usuario(loginOperador);
        Instant agora = Instant.now();
        Convite c = pendenteBloqueado(id, agora);
        c.aprovar(operador, agora);
        return SolicitacaoConviteResponse.from(c, agora);
    }

    @Transactional
    public SolicitacaoConviteResponse recusar(String loginOperador, Long id, String motivo) {
        Usuario operador = usuario(loginOperador);
        Instant agora = Instant.now();
        Convite c = pendenteBloqueado(id, agora);
        String texto = motivo == null || motivo.isBlank() ? null : motivo.trim();
        c.recusar(operador, texto, agora);
        return SolicitacaoConviteResponse.from(c, agora);
    }

    /**
     * Trava a linha (duas decisoes simultaneas nao se sobrepoem) e exige que a solicitacao ainda esteja
     * pendente e dentro do prazo.
     */
    private Convite pendenteBloqueado(Long id, Instant agora) {
        Convite travado = convites.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Solicitacao nao encontrada"));
        if (travado.getStatus() != StatusConvite.PENDENTE_APROVACAO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "SOLICITACAO_JA_DECIDIDA",
                    "Esta solicitacao ja foi decidida (" + travado.getStatus() + ")");
        }
        if (travado.statusEfetivo(agora) == StatusConvite.EXPIRADO) {
            throw ConviteException.regra(HttpStatus.CONFLICT, "SOLICITACAO_EXPIRADA",
                    "O prazo deste convite ja terminou; peca ao morador para criar outro");
        }
        // recarrega com morador/convidado/ambiente para montar a resposta (mesmo persistence context)
        return convites.findComPartesById(id).orElse(travado);
    }

    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
