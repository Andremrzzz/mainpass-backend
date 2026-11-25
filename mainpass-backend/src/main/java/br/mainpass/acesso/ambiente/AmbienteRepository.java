package br.mainpass.acesso.ambiente;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
public interface AmbienteRepository extends JpaRepository<Ambiente,Long> {
    boolean existsByNomeIgnoreCase(String nome);
    Optional<Ambiente> findByNomeIgnoreCase(String nome);
}
