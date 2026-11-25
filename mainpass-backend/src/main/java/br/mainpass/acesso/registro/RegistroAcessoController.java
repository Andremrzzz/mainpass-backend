package br.mainpass.acesso.registro;
import br.mainpass.acesso.registro.dto.RegistroAcessoResponse;
import br.mainpass.acesso.registro.dto.ValidarAcessoRequest;
import br.mainpass.acesso.registro.dto.ValidarAcessoResponse;
import org.springframework.data.domain.*;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.time.Instant;
import jakarta.validation.Valid;
@RestController @RequestMapping("/api/acessos")
public class RegistroAcessoController {
 private final RegistroAcessoService service; private final RegistroAcessoRepository repo;
 public RegistroAcessoController(RegistroAcessoService s,RegistroAcessoRepository r){service=s;repo=r;}
 @PostMapping("/validar") @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
 @ResponseStatus(HttpStatus.OK)
 public ValidarAcessoResponse validar(Authentication auth, @Valid @RequestBody ValidarAcessoRequest req){return service.validar(req.token(),req.ambienteId(),auth.getName());}
 @GetMapping @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
 public Page<RegistroAcessoResponse> historico(@RequestParam(required=false) ResultadoAcesso resultado,
                                               @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant de,
                                               @RequestParam(required=false) @DateTimeFormat(iso=DateTimeFormat.ISO.DATE_TIME) Instant ate,
                                               @PageableDefault(size=20,sort="dataHora",direction=Sort.Direction.DESC) Pageable pageable){
  if(de!=null && ate!=null && de.isAfter(ate)) throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_REQUEST,"Periodo invalido");
  return repo.buscar(resultado,de,ate,pageable).map(RegistroAcessoResponse::from);
 }
}
