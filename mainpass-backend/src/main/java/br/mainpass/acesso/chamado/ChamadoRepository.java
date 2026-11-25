package br.mainpass.acesso.chamado;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChamadoRepository extends JpaRepository<Chamado, Long> {

    @Override
    @EntityGraph(attributePaths = "solicitante")
    Page<Chamado> findAll(Pageable pageable);

    @EntityGraph(attributePaths = "solicitante")
    Page<Chamado> findByStatus(StatusChamado status, Pageable pageable);

    @EntityGraph(attributePaths = "solicitante")
    List<Chamado> findBySolicitanteIdOrderByCriadoEmDesc(Long solicitanteId);

    /** Para o card "Chamados abertos" do painel da portaria. */
    long countByStatus(StatusChamado status);
}
