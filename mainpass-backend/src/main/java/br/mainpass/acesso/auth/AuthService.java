package br.mainpass.acesso.auth;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import br.mainpass.acesso.auth.dto.LoginRequest;
import br.mainpass.acesso.auth.dto.LoginResponse;
import br.mainpass.acesso.usuario.Usuario;
import br.mainpass.acesso.usuario.UsuarioRepository;

@Service
public class AuthService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final JwtService jwt;

    public AuthService(UsuarioRepository usuarios, PasswordEncoder encoder, JwtService jwt) {
        this.usuarios = usuarios; this.encoder = encoder; this.jwt = jwt;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarios.findByEmail(request.email().trim().toLowerCase())
                .filter(Usuario::isAtivo)
                .filter(u -> encoder.matches(request.senha(), u.getSenhaHash()))
                .orElseThrow(() -> new BadCredentialsException("Credenciais invalidas"));
        return LoginResponse.bearer(jwt.gerarToken(usuario), jwt.expiraEmSegundos());
    }

    @Transactional(readOnly = true)
    public Usuario usuarioAtual(String email) {
        return usuarios.findByEmail(email).filter(Usuario::isAtivo)
                .orElseThrow(() -> new BadCredentialsException("Usuario inativo ou inexistente"));
    }
}
