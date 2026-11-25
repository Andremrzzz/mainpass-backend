package br.mainpass.acesso.chamado;

import br.mainpass.acesso.chamado.dto.ChamadoRequest;
import br.mainpass.acesso.chamado.dto.ChamadoResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chamados")
public class ChamadoController {

    private final ChamadoService service;

    public ChamadoController(ChamadoService service) {
        this.service = service;
    }

    /** Qualquer usuario autenticado (ex.: morador) abre um chamado em seu proprio nome. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChamadoResponse abrir(Authentication auth, @Valid @RequestBody ChamadoRequest req) {
        return service.abrir(auth.getName(), req);
    }

    /** Chamados do proprio usuario (para ele acompanhar o status). */
    @GetMapping("/meus")
    public List<ChamadoResponse> meus(Authentication auth) {
        return service.meus(auth.getName());
    }

    /** Caixa de entrada da portaria. Filtro opcional: ?status=ABERTO | RESOLVIDO. */
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public Page<ChamadoResponse> listar(
            @RequestParam(required = false) StatusChamado status,
            @PageableDefault(size = 20, sort = "criadoEm", direction = Sort.Direction.DESC) Pageable pageable) {
        return service.listar(status, pageable);
    }

    @PutMapping("/{id}/resolver")
    @PreAuthorize("hasAnyRole('ADMIN','OPERADOR')")
    public ChamadoResponse resolver(Authentication auth, @PathVariable Long id) {
        return service.resolver(id, auth.getName());
    }
}
