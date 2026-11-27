package br.mainpass.acesso.registro;

import java.time.Instant;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RegistroAcessoRepository extends JpaRepository<RegistroAcesso, Long> {

    Page<RegistroAcesso> findByUsuarioId(Long usuarioId, Pageable pageable);

    /** Filtros opcionais: passe null para ignorar. A ordenacao vem do Pageable (ex.: sort=dataHora,desc). */
    @Query("""
            select r from RegistroAcesso r
                where (:resultado is null or r.resultado = :resultado)
                    and (:de is null or r.dataHora >= :de)
                    and (:ate is null or r.dataHora <= :ate)
            """)
    Page<RegistroAcesso> buscar(@Param("resultado") ResultadoAcesso resultado,
                                @Param("de") Instant de,
                                @Param("ate") Instant ate,
                                Pageable pageable);

    @Query("""
        select r
        from RegistroAcesso r
        join r.qrCode qr
        left join fetch r.ambiente
        where r.usuario.id = :usuarioId
          and qr.usuario.id = :usuarioId
        """)
    Page<RegistroAcesso> buscarEntradasDoConvidado(
            @Param("usuarioId") Long usuarioId,
            Pageable pageable
    );
}


