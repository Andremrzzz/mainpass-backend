package br.mainpass.acesso.usuario;

import java.util.List;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import br.mainpass.acesso.common.exception.BusinessException;
import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.usuario.dto.UsuarioRequest;
import br.mainpass.acesso.usuario.dto.UsuarioResponse;

@Service
public class UsuarioService {
    private final UsuarioRepository repository;
    private final PasswordEncoder encoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder encoder) {
        this.repository = repository;
        this.encoder = encoder;
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar() {
        return repository.findAll().stream().map(UsuarioResponse::from).toList();
    }

    @Transactional
    public UsuarioResponse criar(UsuarioRequest req) {
        String email = req.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw BusinessException.conflict("EMAIL_DUPLICADO", "E-mail ja cadastrado");
        }
        if (req.senha() == null || req.senha().isBlank()) {
            throw BusinessException.badRequest("SENHA_OBRIGATORIA", "Senha e obrigatoria no cadastro");
        }

        Usuario u = new Usuario();
        u.setNome(req.nome().trim());
        u.setEmail(email);
        u.setSenhaHash(encoder.encode(req.senha()));
        u.setPerfil(req.perfil());
        u.setCpf(limparTexto(req.cpf()));
        u.setTelefone(limparTexto(req.telefone()));
        u.setBloco(limparTexto(req.bloco()));
        u.setUnidade(limparTexto(req.unidade()));
        u.setAtivo(true);

        return UsuarioResponse.from(repository.save(u));
    }

    @Transactional
    public UsuarioResponse atualizar(Long id, UsuarioRequest req) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));

        String email = req.email().trim().toLowerCase();
        if (!u.getEmail().equals(email) && repository.existsByEmail(email)) {
            throw BusinessException.conflict("EMAIL_DUPLICADO", "E-mail ja cadastrado");
        }

        u.setNome(req.nome().trim());
        u.setEmail(email);
        if (req.senha() != null && !req.senha().isBlank()) {
            u.setSenhaHash(encoder.encode(req.senha()));
        }
        u.setPerfil(req.perfil());
        u.setCpf(limparTexto(req.cpf()));
        u.setTelefone(limparTexto(req.telefone()));
        u.setBloco(limparTexto(req.bloco()));
        u.setUnidade(limparTexto(req.unidade()));

        return UsuarioResponse.from(u);
    }

    @Transactional
    public void alterarStatus(Long id, boolean ativo) {
        Usuario u = repository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario", id));
        u.setAtivo(ativo);
    }

    private String limparTexto(String valor) {
        if (valor == null) return null;
        String trimmed = valor.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}