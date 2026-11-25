package br.mainpass.acesso.portaria;

import br.mainpass.acesso.usuario.Perfil;
import br.mainpass.acesso.usuario.Usuario;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/** Consultas de moradores para a portaria (repositorio proprio: nao mexe no UsuarioRepository). */
public interface MoradorPortariaRepository extends JpaRepository<Usuario, Long> {

    /**
     * Busca por nome, e-mail, bloco, unidade, "bloco unidade" ou CPF (so digitos).
     * `texto` e `cpf` sao padroes LIKE ja montados pelo service ("%termo%"); `cpf` recebe um valor que nunca
     * casa quando o termo nao tem digitos suficientes.
     */
    @Query("""
            select u from Usuario u
            where u.perfil = :perfil
              and (lower(u.nome) like :texto
                   or lower(u.email) like :texto
                   or lower(coalesce(u.bloco, '')) like :texto
                   or lower(coalesce(u.unidade, '')) like :texto
                   or lower(concat(coalesce(u.bloco, ''), ' ', coalesce(u.unidade, ''))) like :texto
                   or replace(replace(coalesce(u.cpf, ''), '.', ''), '-', '') like :cpf)
            """)
    Page<Usuario> buscar(@Param("perfil") Perfil perfil, @Param("texto") String texto,
                         @Param("cpf") String cpf, Pageable pageable);
}
