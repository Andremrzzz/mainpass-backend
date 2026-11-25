package br.mainpass.acesso.chamado;

import br.mainpass.acesso.chamado.dto.ChamadoRequest;
import br.mainpass.acesso.chamado.dto.ChamadoResponse;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ChamadoService {

    private final ChamadoRepository chamados;
    private final UsuarioRepository usuarios;

    public ChamadoService(ChamadoRepository chamados, UsuarioRepository usuarios) {
        this.chamados = chamados;
        this.usuarios = usuarios;
    }

    @Transactional
    public ChamadoResponse abrir(String login, ChamadoRequest req) {
        Usuario solicitante = usuario(login);
        Chamado chamado = new Chamado(solicitante, req.assunto().trim(), req.mensagem().trim());
        return ChamadoResponse.from(chamados.save(chamado));
    }

    @Transactional(readOnly = true)
    public List<ChamadoResponse> meus(String login) {
        Usuario solicitante = usuario(login);
        return chamados.findBySolicitanteIdOrderByCriadoEmDesc(solicitante.getId())
                .stream().map(ChamadoResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public Page<ChamadoResponse> listar(StatusChamado status, Pageable pageable) {
        Page<Chamado> pagina = status == null ? chamados.findAll(pageable) : chamados.findByStatus(status, pageable);
        return pagina.map(ChamadoResponse::from);
    }

    @Transactional
    public ChamadoResponse resolver(Long id, String login) {
        Chamado chamado = chamados.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Chamado nao encontrado"));
        chamado.resolver(usuario(login));
        return ChamadoResponse.from(chamado);
    }

    // PREMISSA: auth.getName() e o login do usuario (o mesmo que o JwtFilter usa para carregar o Usuario).
    // Ajuste findByEmail para o metodo real do seu UsuarioRepository.
    private Usuario usuario(String login) {
        return usuarios.findByEmail(login)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario nao encontrado"));
    }
}
