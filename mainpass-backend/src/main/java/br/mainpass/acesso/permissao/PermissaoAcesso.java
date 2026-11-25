package br.mainpass.acesso.permissao;

import java.time.Instant;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** Permissao de um usuario em um ambiente, com janela de vigencia. */
@Entity
@Table(name = "permissao_acesso")
@Getter
@Setter
@NoArgsConstructor
public class PermissaoAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ambiente_id", nullable = false)
    private Ambiente ambiente;

    @Column(name = "valido_de", nullable = false)
    private Instant validoDe;

    /** Nulo significa sem data de termino. */
    @Column(name = "valido_ate")
    private Instant validoAte;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @PrePersist
    void antesDeSalvar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
        if (validoDe == null) {
            validoDe = criadoEm;
        }
    }

    public boolean vigenteEm(Instant momento) {
        return !momento.isBefore(validoDe) && (validoAte == null || !momento.isAfter(validoAte));
    }
}
