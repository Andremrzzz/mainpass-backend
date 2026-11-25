package br.mainpass.acesso.permissao;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface PermissaoAcessoRepository extends JpaRepository<PermissaoAcesso,Long> {
 Optional<PermissaoAcesso> findByUsuarioIdAndAmbienteId(Long usuarioId,Long ambienteId);
 List<PermissaoAcesso> findByUsuarioId(Long usuarioId);
 boolean existsByUsuarioIdAndAmbienteId(Long usuarioId,Long ambienteId);
}
