package br.mainpass.acesso.auth;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import br.mainpass.acesso.auth.dto.LoginRequest;
import br.mainpass.acesso.auth.dto.LoginResponse;
import br.mainpass.acesso.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService service;
    public AuthController(AuthService service) { this.service = service; }

    @PostMapping("/login")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) { return service.login(request); }

    @GetMapping("/me")
    public UsuarioResponse me(Authentication authentication) {
        return UsuarioResponse.from(service.usuarioAtual(authentication.getName()));
    }
}
