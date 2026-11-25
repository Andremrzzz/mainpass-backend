package br.mainpass.acesso.registro;

import java.time.Instant;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.qrcode.QrCode;
import br.mainpass.acesso.usuario.Usuario;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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

/** Log de toda tentativa de validacao: autorizada, recusada ou com codigo invalido. */
@Entity
@Table(name = "registro_acesso")
@Getter
@Setter
@NoArgsConstructor
public class RegistroAcesso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Nulo quando o codigo lido nao existe. */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "qr_code_id")
    private QrCode qrCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ambiente_id")
    private Ambiente ambiente;

    /** Quem fez a leitura (OPERADOR/ADMIN logado). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "operador_id", nullable = false)
    private Usuario operador;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ResultadoAcesso resultado;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private MotivoRecusa motivo;

    @Column(name = "data_hora", nullable = false)
    private Instant dataHora;

    @PrePersist
    void antesDeSalvar() {
        if (dataHora == null) {
            dataHora = Instant.now();
        }
    }
}
