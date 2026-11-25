package br.mainpass.acesso.ambiente;
import java.util.List;

import br.mainpass.acesso.ambiente.dto.AmbienteRequest;
import br.mainpass.acesso.ambiente.dto.AmbienteResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import br.mainpass.acesso.ambiente.dto.*;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/ambientes")
public class AmbienteController {
 private final AmbienteService service; public AmbienteController(AmbienteService service){this.service=service;}
 @GetMapping public List<AmbienteResponse> listar(){return service.listar();}
 @PostMapping @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.CREATED) public AmbienteResponse criar(@Valid @RequestBody AmbienteRequest r){return service.criar(r);}
 @PutMapping("/{id}") @PreAuthorize("hasRole('ADMIN')") public AmbienteResponse atualizar(@PathVariable Long id,@Valid @RequestBody AmbienteRequest r){return service.atualizar(id,r);}
 @PatchMapping("/{id}/status") @PreAuthorize("hasRole('ADMIN')") @ResponseStatus(HttpStatus.NO_CONTENT) public void status(@PathVariable Long id,@RequestParam boolean ativo){service.status(id,ativo);}
}
