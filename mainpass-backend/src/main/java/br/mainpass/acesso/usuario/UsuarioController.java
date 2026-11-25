package br.mainpass.acesso.usuario;

import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.mainpass.acesso.usuario.dto.UsuarioRequest;
import br.mainpass.acesso.usuario.dto.UsuarioResponse;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {
    private final UsuarioService service;
    public UsuarioController(UsuarioService service) { this.service=service; }
    @GetMapping public List<UsuarioResponse> listar(){ return service.listar(); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest req){ return service.criar(req); }
    @PutMapping("/{id}") public UsuarioResponse atualizar(@PathVariable Long id,@Valid @RequestBody UsuarioRequest req){ return service.atualizar(id,req); }
    @PatchMapping("/{id}/status") @ResponseStatus(HttpStatus.NO_CONTENT)
    public void status(@PathVariable Long id,@RequestParam boolean ativo){ service.alterarStatus(id,ativo); }
}
