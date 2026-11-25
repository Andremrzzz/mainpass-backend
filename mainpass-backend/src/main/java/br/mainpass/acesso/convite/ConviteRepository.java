package br.mainpass.acesso.convite;

import jakarta.persistence.LockModeType;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ConviteRepository extends JpaRepository<Convite, Long> {

    /* ------------------------------ Morador ------------------------------ */

    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    Page<Convite> findByMoradorId(Long moradorId, Pageable pageable);

    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    Page<Convite> findByMoradorIdAndStatus(Long moradorId, StatusConvite status, Pageable pageable);

    /** ATIVO "de verdade": persistido como ATIVO e ainda dentro da validade. */
    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    @Query("select c from Convite c where c.morador.id = :moradorId and c.status = :ativo and c.dataFim > :agora")
    Page<Convite> findVigentesDoMorador(@Param("moradorId") Long moradorId, @Param("ativo") StatusConvite ativo,
                                        @Param("agora") Instant agora, Pageable pageable);

    /** EXPIRADO persistido ou ATIVO com validade vencida. */
    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    @Query("""
            select c from Convite c
            where c.morador.id = :moradorId
              and (c.status = :expirado or (c.status = :ativo and c.dataFim <= :agora))
            """)
    Page<Convite> findExpiradosDoMorador(@Param("moradorId") Long moradorId, @Param("ativo") StatusConvite ativo,
                                         @Param("expirado") StatusConvite expirado,
                                         @Param("agora") Instant agora, Pageable pageable);

    long countByMoradorIdAndStatusAndDataFimAfter(Long moradorId, StatusConvite status, Instant agora);

    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    @Query("select c from Convite c where c.id = :id")
    Optional<Convite> findDetalhadoById(@Param("id") Long id);

    /* ------------------------------ Portaria ------------------------------ */

    /** Todos os convites de um morador (mais recentes primeiro), com convidado e ambiente ja carregados (sem N+1). */
    @EntityGraph(attributePaths = {"convidado", "ambiente"})
    @Query("select c from Convite c where c.morador.id = :moradorId order by c.criadoEm desc")
    List<Convite> findPorMoradorComPartes(@Param("moradorId") Long moradorId, Pageable pageable);

    /** [moradorId, quantidade] de convites vigentes, em UMA consulta para a pagina inteira de moradores. */
    @Query("""
            select c.morador.id, count(c) from Convite c
            where c.morador.id in :ids and c.status = :ativo and c.dataFim > :agora
            group by c.morador.id
            """)
    List<Object[]> contarVigentesPorMorador(@Param("ids") java.util.Collection<Long> ids,
                                            @Param("ativo") StatusConvite ativo, @Param("agora") Instant agora);

    /* ----------------------------- Convidado ----------------------------- */

    @EntityGraph(attributePaths = {"morador", "ambiente"})
    @Query("select c from Convite c where c.convidado.id = :convidadoId order by c.dataInicio desc")
    List<Convite> findDoConvidado(@Param("convidadoId") Long convidadoId, Pageable pageable);

    /* ----------------------------- Validacao ----------------------------- */

    /** Bloqueia a linha ate o fim da transacao: dois leitores simultaneos nao consomem o mesmo uso. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Convite c where c.id = :id")
    Optional<Convite> findByIdForUpdate(@Param("id") Long id);
}
