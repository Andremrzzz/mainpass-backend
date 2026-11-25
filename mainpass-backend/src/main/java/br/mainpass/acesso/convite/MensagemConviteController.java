package br.mainpass.acesso.convite;

import br.mainpass.acesso.convite.dto.MensagemRequest;
import br.mainpass.acesso.convite.dto.MensagemResponse;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/** Chat do convite. Morador e convidado usam as mesmas rotas; a autorizacao fina (participante) fica no service. */
@RestController
@RequestMapping("/api/convites/{conviteId}/mensagens")
@PreAuthorize("hasAnyRole('USUARIO','CONVIDADO')")
public class MensagemConviteController {

    private final MensagemConviteService service;

    public MensagemConviteController(MensagemConviteService service) {
        this.service = service;
    }

    /** Ordenadas da mais antiga para a mais recente. `?apos=<ultimoId>` devolve so as novas (polling). */
    @GetMapping
    public List<MensagemResponse> listar(Authentication auth, @PathVariable Long conviteId,
                                         @RequestParam(required = false) Long apos) {
        return service.listar(auth.getName(), conviteId, apos);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public MensagemResponse enviar(Authentication auth, @PathVariable Long conviteId,
                                   @Valid @RequestBody MensagemRequest req) {
        return service.enviar(auth.getName(), conviteId, req.conteudo());
    }

    /** Marca como lidas as mensagens da outra parte. */
    @PutMapping("/lidas")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void marcarComoLidas(Authentication auth, @PathVariable Long conviteId) {
        service.marcarComoLidas(auth.getName(), conviteId);
    }
}
