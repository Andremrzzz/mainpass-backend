package br.mainpass.acesso.portaria;

import br.mainpass.acesso.common.exception.ResourceNotFoundException;
import br.mainpass.acesso.convite.ConviteRepository;
import br.mainpass.acesso.convite.StatusConvite;
import br.mainpass.acesso.portaria.dto.ConviteDoMoradorResponse;
import br.mainpass.acesso.portaria.dto.MoradorPortariaDetalheResponse;
import br.mainpass.acesso.portaria.dto.MoradorPortariaResponse;
import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta de moradores e convidados para a portaria. Somente leitura. */
@Service
@Transactional(readOnly = true)
public class PortariaMoradorService {

    static final int TAMANHO_MAXIMO_PAGINA = 100;
    static final int MAX_CONVITES_POR_MORADOR = 200;
    private static final String SEM_CORRESPONDENCIA = "#nenhum#";

    private final MoradorPortariaRepository moradores;
    private final ConviteRepository convites;

    public PortariaMoradorService(MoradorPortariaRepository moradores, ConviteRepository convites) {
        this.moradores = moradores;
        this.convites = convites;
    }

    public Page<MoradorPortariaResponse> listar(String busca, int pagina, int tamanho) {
        String termo = busca == null ? "" : busca.trim().toLowerCase(Locale.ROOT).replaceAll("[%_\\\\]", "");
        String digitos = termo.replaceAll("\\D", "");
        String padraoTexto = "%" + termo + "%";
        String padraoCpf = digitos.length() >= 3 ? "%" + digitos + "%" : SEM_CORRESPONDENCIA;

        var pageable = PageRequest.of(Math.max(pagina, 0), Math.min(Math.max(tamanho, 1), TAMANHO_MAXIMO_PAGINA),
                Sort.by(Sort.Direction.ASC, "nome").and(Sort.by("id")));
        Page<Usuario> resultado = moradores.buscar(Perfil.USUARIO, padraoTexto, padraoCpf, pageable);

        // Uma unica consulta agrupada para a pagina inteira (evita N+1)
        Map<Long, Long> vigentes = new HashMap<>();
        if (!resultado.isEmpty()) {
            var ids = resultado.getContent().stream().map(Usuario::getId).toList();
            for (Object[] linha : convites.contarVigentesPorMorador(ids, StatusConvite.ATIVO, Instant.now())) {
                vigentes.put((Long) linha[0], (Long) linha[1]);
            }
        }
        return resultado.map(u -> MoradorPortariaResponse.from(u, vigentes.getOrDefault(u.getId(), 0L)));
    }

    public MoradorPortariaDetalheResponse detalhar(Long moradorId) {
        Usuario morador = morador(moradorId);
        List<ConviteDoMoradorResponse> lista = listaConvidados(morador);
        long vigentes = lista.stream().filter(c -> c.status() == StatusConvite.ATIVO).count();
        return new MoradorPortariaDetalheResponse(MoradorPortariaResponse.from(morador, vigentes), lista);
    }

    public List<ConviteDoMoradorResponse> convidados(Long moradorId) {
        return listaConvidados(morador(moradorId));
    }

    /* ------------------------------ internos ------------------------------ */

    private List<ConviteDoMoradorResponse> listaConvidados(Usuario morador) {
        Instant agora = Instant.now();
        return convites.findPorMoradorComPartes(morador.getId(), PageRequest.ofSize(MAX_CONVITES_POR_MORADOR))
                .stream().map(c -> ConviteDoMoradorResponse.from(c, agora)).toList();
    }

    /** Id de quem nao e morador (operador, admin, convidado) responde 404, como se nao existisse. */
    private Usuario morador(Long id) {
        return moradores.findById(id)
                .filter(u -> u.getPerfil() == Perfil.USUARIO)
                .orElseThrow(() -> new ResourceNotFoundException("Morador nao encontrado"));
    }
}
