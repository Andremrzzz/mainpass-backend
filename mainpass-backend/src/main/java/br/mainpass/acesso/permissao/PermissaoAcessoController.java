package br.mainpass.acesso.permissao;
import java.util.List;

import br.mainpass.acesso.permissao.dto.PermissaoRequest;
import br.mainpass.acesso.permissao.dto.PermissaoResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.mainpass.acesso.permissao.dto.*;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/permissoes") @PreAuthorize("hasRole('ADMIN')")
public class PermissaoAcessoController {
 private final PermissaoAcessoService service; public PermissaoAcessoController(PermissaoAcessoService s){service=s;}
 @GetMapping public List<PermissaoResponse> listar(){return service.listar();}
 @PostMapping @ResponseStatus(HttpStatus.CREATED) public PermissaoResponse conceder(@Valid @RequestBody PermissaoRequest r){return service.conceder(r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void revogar(@PathVariable Long id){service.revogar(id);}
}
