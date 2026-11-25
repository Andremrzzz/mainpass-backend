package br.mainpass.acesso.portaria;

import br.mainpass.acesso.portaria.dto.ConviteDoMoradorResponse;
import br.mainpass.acesso.portaria.dto.MoradorPortariaDetalheResponse;
import br.mainpass.acesso.portaria.dto.MoradorPortariaResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Consulta de moradores e seus convidados para o porteiro (OPERADOR) e o ADMIN. Somente leitura. */
@RestController
@RequestMapping("/api/portaria/moradores")
@PreAuthorize("hasAnyRole('OPERADOR','ADMIN')")
public class PortariaMoradorController {

    private final PortariaMoradorService service;

    public PortariaMoradorController(PortariaMoradorService service) {
        this.service = service;
    }

    /** `?q=` filtra por nome, e-mail, bloco, unidade ou CPF (digitos). Ordenacao fixa por nome. */
    @GetMapping
    public Page<MoradorPortariaResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listar(q, page, size);
    }

    /** Morador + todos os convidados (convites), numa unica chamada. */
    @GetMapping("/{moradorId}")
    public MoradorPortariaDetalheResponse detalhar(@PathVariable Long moradorId) {
        return service.detalhar(moradorId);
    }

    @GetMapping("/{moradorId}/convidados")
    public List<ConviteDoMoradorResponse> convidados(@PathVariable Long moradorId) {
        return service.convidados(moradorId);
    }
}
