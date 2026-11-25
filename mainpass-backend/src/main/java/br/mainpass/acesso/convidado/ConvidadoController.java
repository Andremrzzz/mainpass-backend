package br.mainpass.acesso.convidado;

import br.mainpass.acesso.convidado.dto.ConvidadoPerfilResponse;
import br.mainpass.acesso.convidado.dto.ConvidadoQrCodeResponse;
import br.mainpass.acesso.convidado.dto.EntradaResponse;
import br.mainpass.acesso.convidado.dto.SolicitarRenovacaoRequest;
import br.mainpass.acesso.convite.dto.MensagemResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Area do CONVIDADO: tudo e filtrado pelo usuario do token; nenhum id de usuario vem do cliente. */
@RestController
@RequestMapping("/api/convidado")
@PreAuthorize("hasRole('CONVIDADO')")
public class ConvidadoController {

    private final ConvidadoService service;

    public ConvidadoController(ConvidadoService service) {
        this.service = service;
    }

    @GetMapping("/perfil")
    public ConvidadoPerfilResponse perfil(Authentication auth) {
        return service.perfil(auth.getName());
    }

    /** Convites/QR Codes do convidado (ativos e historicos). O status ja vem calculado. */
    @GetMapping("/qrcodes")
    public List<ConvidadoQrCodeResponse> qrcodes(Authentication auth) {
        return service.qrcodes(auth.getName());
    }

    @GetMapping("/entradas")
    public Page<EntradaResponse> entradas(Authentication auth,
                                          @RequestParam(defaultValue = "0") int page,
                                          @RequestParam(defaultValue = "20") int size) {
        return service.entradas(auth.getName(), page, size);
    }

    @PostMapping("/convites/{id}/solicitar-renovacao")
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse solicitarRenovacao(Authentication auth, @PathVariable Long id,
                                               @Valid @RequestBody(required = false) SolicitarRenovacaoRequest req) {
        return service.solicitarRenovacao(auth.getName(), id, req);
    }
}
