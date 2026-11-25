package br.mainpass.acesso.qrcode;
import java.time.Instant;
import java.util.*;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
public interface QrCodeRepository extends JpaRepository<QrCode,Long> {
 Optional<QrCode> findByTokenHash(String tokenHash);
 List<QrCode> findByUsuarioIdOrderByCriadoEmDesc(Long usuarioId);
 @Modifying(flushAutomatically=true)
 @Query("update QrCode q set q.status=:novo,q.utilizadoEm=:agora where q.id=:id and q.status=:esperado")
 int atualizarStatus(@Param("id") Long id,@Param("esperado") StatusQrCode esperado,@Param("novo") StatusQrCode novo,@Param("agora") Instant agora);
}
