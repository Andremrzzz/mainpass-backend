package br.mainpass.acesso.auth;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** Cadastro publico: sempre cria uma conta de CONVIDADO. Rota liberada no SecurityConfig. */
@RestController
@RequestMapping("/api/auth")
public class CadastroController {

    private final CadastroService service;

    public CadastroController(CadastroService service) {
        this.service = service;
    }

    @PostMapping("/cadastro")
    @ResponseStatus(HttpStatus.CREATED)
    public CadastroResponse cadastrar(@Valid @RequestBody CadastroRequest req) {
        return CadastroResponse.from(service.cadastrar(req));
    }
}
