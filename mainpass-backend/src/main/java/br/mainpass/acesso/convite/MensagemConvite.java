package br.mainpass.acesso.convite;

import br.mainpass.acesso.usuario.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "mensagens_convite")
public class MensagemConvite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convite_id", nullable = false)
    private Convite convite;

    /** Sempre o usuario do token, nunca um id vindo do corpo da requisicao. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "remetente_id", nullable = false)
    private Usuario remetente;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 25)
    private TipoMensagem tipo = TipoMensagem.TEXTO;

    @Column(nullable = false, length = 500)
    private String conteudo;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    /** Lida pelo DESTINATARIO (a outra parte do convite). */
    @Column(nullable = false)
    private boolean lida;

    protected MensagemConvite() {
    }

    public MensagemConvite(Convite convite, Usuario remetente, TipoMensagem tipo, String conteudo) {
        this.convite = convite;
        this.remetente = remetente;
        this.tipo = tipo;
        this.conteudo = conteudo;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    public Long getId() { return id; }
    public Convite getConvite() { return convite; }
    public Usuario getRemetente() { return remetente; }
    public TipoMensagem getTipo() { return tipo; }
    public String getConteudo() { return conteudo; }
    public Instant getCriadoEm() { return criadoEm; }
    public boolean isLida() { return lida; }
}
