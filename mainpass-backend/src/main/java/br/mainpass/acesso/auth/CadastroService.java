package br.mainpass.acesso.auth;

import br.mainpass.acesso.convite.ConviteException;
import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;
import java.util.Locale;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Auto-cadastro. REGRA: o perfil e SEMPRE {@link Perfil#CONVIDADO}, definido aqui no servidor.
 * Nao existe parametro nem caminho para escolher outro perfil; Morador (USUARIO), OPERADOR e ADMIN
 * so sao criados por usuarios autenticados com permissao (portaria/admin).
 */
@Service
public class CadastroService {

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;

    public CadastroService(UsuarioRepository usuarios, PasswordEncoder encoder) {
        this.usuarios = usuarios;
        this.encoder = encoder;
    }

    @Transactional
    public Usuario cadastrar(CadastroRequest req) {
        String email = req.email().trim().toLowerCase(Locale.ROOT);
        if (usuarios.findByEmail(email).isPresent()) {
            throw emailEmUso();
        }

        Usuario novo = new Usuario(); // Lombok @NoArgsConstructor/@Setter
        novo.setNome(req.nome().trim().replaceAll("\\s+", " "));
        novo.setEmail(email);
        novo.setSenhaHash(encoder.encode(req.senha()));
        novo.setPerfil(Perfil.CONVIDADO); // fixo: nunca vem do cliente
        novo.setAtivo(true);
        try {
            return usuarios.saveAndFlush(novo);
        } catch (DataIntegrityViolationException e) { // dois cadastros simultaneos com o mesmo e-mail
            throw emailEmUso();
        }
    }

    private static ConviteException emailEmUso() {
        return ConviteException.regra(HttpStatus.CONFLICT, "EMAIL_EM_USO", "Este e-mail ja esta cadastrado");
    }
}
