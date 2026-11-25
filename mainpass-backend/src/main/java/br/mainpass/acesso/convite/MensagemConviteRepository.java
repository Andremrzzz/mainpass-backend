package br.mainpass.acesso.convite;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MensagemConviteRepository extends JpaRepository<MensagemConvite, Long> {

    /** Ordem por id == ordem de criacao; `apos` permite polling incremental (0 = tudo). */
    @EntityGraph(attributePaths = "remetente")
    List<MensagemConvite> findByConviteIdAndIdGreaterThanOrderByIdAsc(Long conviteId, Long apos, Pageable pageable);

    /** Marca como lidas as mensagens enviadas pela OUTRA parte. */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
            update MensagemConvite m set m.lida = true
            where m.convite.id = :conviteId and m.remetente.id <> :leitorId and m.lida = false
            """)
    int marcarComoLidas(@Param("conviteId") Long conviteId, @Param("leitorId") Long leitorId);
}
