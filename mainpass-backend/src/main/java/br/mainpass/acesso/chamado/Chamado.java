package br.mainpass.acesso.chamado;

import br.mainpass.acesso.usuario.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "chamados", indexes = @Index(name = "idx_chamados_status", columnList = "status"))
public class Chamado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Quem abriu o chamado: sempre o usuario do token, nunca um id vindo do corpo da requisicao. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "solicitante_id", nullable = false)
    private Usuario solicitante;

    @Column(nullable = false, length = 150)
    private String assunto;

    @Column(nullable = false, length = 2000)
    private String mensagem;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusChamado status = StatusChamado.ABERTO;

    @Column(nullable = false, updatable = false)
    private Instant criadoEm;

    private Instant resolvidoEm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "resolvido_por_id")
    private Usuario resolvidoPor;

    protected Chamado() {
    }

    public Chamado(Usuario solicitante, String assunto, String mensagem) {
        this.solicitante = solicitante;
        this.assunto = assunto;
        this.mensagem = mensagem;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    /** Idempotente: resolver um chamado ja resolvido nao altera data nem responsavel. */
    public void resolver(Usuario responsavel) {
        if (status == StatusChamado.RESOLVIDO) {
            return;
        }
        status = StatusChamado.RESOLVIDO;
        resolvidoEm = Instant.now();
        resolvidoPor = responsavel;
    }

    public Long getId() { return id; }
    public Usuario getSolicitante() { return solicitante; }
    public String getAssunto() { return assunto; }
    public String getMensagem() { return mensagem; }
    public StatusChamado getStatus() { return status; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getResolvidoEm() { return resolvidoEm; }
    public Usuario getResolvidoPor() { return resolvidoPor; }
}
