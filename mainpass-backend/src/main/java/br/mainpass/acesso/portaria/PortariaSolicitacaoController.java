package br.mainpass.acesso.portaria;

import br.mainpass.acesso.portaria.dto.RecusarSolicitacaoRequest;
import br.mainpass.acesso.portaria.dto.SolicitacaoConviteResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Aprovacao de convites pela portaria. Somente OPERADOR (porteiro) e ADMIN. */
@RestController
@RequestMapping("/api/portaria/convites")
@PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
public class PortariaSolicitacaoController {

    private final PortariaSolicitacaoService service;

    public PortariaSolicitacaoController(PortariaSolicitacaoService service) {
        this.service = service;
    }

    /** Fila: solicitacoes aguardando decisao (mais antigas primeiro). */
    @GetMapping("/pendentes")
    public List<SolicitacaoConviteResponse> pendentes() {
        return service.pendentes();
    }

    @PostMapping("/{id}/aprovar")
    public SolicitacaoConviteResponse aprovar(Authentication auth, @PathVariable Long id) {
        return service.aprovar(auth.getName(), id);
    }

    /** Corpo opcional: {"motivo": "..."}. */
    @PostMapping("/{id}/recusar")
    public SolicitacaoConviteResponse recusar(Authentication auth, @PathVariable Long id,
                                              @Valid @RequestBody(required = false) RecusarSolicitacaoRequest req) {
        return service.recusar(auth.getName(), id, req == null ? null : req.motivo());
    }
}
