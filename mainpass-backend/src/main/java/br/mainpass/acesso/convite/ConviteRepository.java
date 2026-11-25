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
