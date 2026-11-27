package br.mainpass.acesso.convite;

import br.mainpass.acesso.convite.dto.ConviteRequest;
import br.mainpass.acesso.convite.dto.ConviteResponse;
import br.mainpass.acesso.convite.dto.RenovarConviteRequest;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Convites do morador (perfil USUARIO, o anfitriao). O morador so enxerga e altera os proprios convites. */
@RestController
@RequestMapping("/api/convites")
@PreAuthorize("hasRole('USUARIO')")
public class ConviteController {

    private final ConviteService service;

    public ConviteController(ConviteService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ConviteResponse criar(Authentication auth, @Valid @RequestBody ConviteRequest req) {
        return service.criar(auth.getName(), req);
    }

    /** Filtro opcional ?status=PENDENTE_APROVACAO|RECUSADO|ATIVO|UTILIZADO|EXPIRADO|CANCELADO. A ordenacao e fixa (mais recentes primeiro). */
    @GetMapping
    public Page<ConviteResponse> listar(
            Authentication auth,
            @RequestParam(required = false) StatusConvite status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(auth.getName(), status, page, size);
    }

    @GetMapping("/{id}")
    public ConviteResponse detalhar(Authentication auth, @PathVariable Long id) {
        return service.detalhar(auth.getName(), id);
    }

    @PatchMapping("/{id}/cancelar")
    public ConviteResponse cancelar(Authentication auth, @PathVariable Long id) {
        return service.cancelar(auth.getName(), id);
    }

    /** Extra: concede mais tempo (resposta a POST /api/convidado/convites/{id}/solicitar-renovacao). */
    @PatchMapping("/{id}/renovar")
    public ConviteResponse renovar(Authentication auth, @PathVariable Long id, @Valid @RequestBody RenovarConviteRequest req) {
        return service.renovar(auth.getName(), id, req);
    }
}
