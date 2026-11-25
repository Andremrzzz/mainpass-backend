package br.mainpass.acesso.convite;

import br.mainpass.acesso.ambiente.Ambiente;
import br.mainpass.acesso.usuario.Usuario;
import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "convites")
public class Convite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Anfitriao: quem criou o convite (sempre o usuario do token). */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "morador_id", nullable = false)
    private Usuario morador;

    /** Usuario com perfil CONVIDADO que recebe o acesso. */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "convidado_id", nullable = false)
    private Usuario convidado;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "ambiente_id", nullable = false)
    private Ambiente ambiente;

    @Column(name = "data_inicio", nullable = false)
    private Instant dataInicio;

    @Column(name = "data_fim", nullable = false)
    private Instant dataFim;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 15)
    private StatusConvite status = StatusConvite.ATIVO;

    @Column(nullable = false)
    private int usos;

    @Column(name = "usos_max", nullable = false)
    private int usosMax;

    @Column(length = 255)
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private Instant criadoEm;

    @Column(name = "cancelado_em")
    private Instant canceladoEm;

    protected Convite() {
    }

    public Convite(Usuario morador, Usuario convidado, Ambiente ambiente,
                   Instant dataInicio, Instant dataFim, int usosMax, String observacao) {
        this.morador = morador;
        this.convidado = convidado;
        this.ambiente = ambiente;
        this.dataInicio = dataInicio;
        this.dataFim = dataFim;
        this.usosMax = usosMax;
        this.observacao = observacao;
    }

    @PrePersist
    void aoCriar() {
        if (criadoEm == null) {
            criadoEm = Instant.now();
        }
    }

    /** Estado que o cliente deve ver: ATIVO vencido aparece como EXPIRADO, sem precisar de job. */
    public StatusConvite statusEfetivo(Instant agora) {
        if (status == StatusConvite.ATIVO && !agora.isBefore(dataFim)) {
            return StatusConvite.EXPIRADO;
        }
        return status;
    }

    /** Consome um uso; ao esgotar, o convite passa a UTILIZADO. */
    public void registrarUso() {
        usos++;
        if (usos >= usosMax) {
            status = StatusConvite.UTILIZADO;
        }
    }

    /** Idempotente. */
    public void cancelar(Instant agora) {
        if (status == StatusConvite.CANCELADO) {
            return;
        }
        status = StatusConvite.CANCELADO;
        canceladoEm = agora;
    }

    /** Estende a validade (e, opcionalmente, os usos). Reativa convites utilizados/expirados que voltem a ter saldo. */
    public void estender(Instant novoFim, int usosAdicionais) {
        dataFim = novoFim;
        usosMax += usosAdicionais;
        if ((status == StatusConvite.UTILIZADO || status == StatusConvite.EXPIRADO) && usos < usosMax) {
            status = StatusConvite.ATIVO;
        }
    }

    public Long getId() { return id; }
    public Usuario getMorador() { return morador; }
    public Usuario getConvidado() { return convidado; }
    public Ambiente getAmbiente() { return ambiente; }
    public Instant getDataInicio() { return dataInicio; }
    public Instant getDataFim() { return dataFim; }
    public StatusConvite getStatus() { return status; }
    public int getUsos() { return usos; }
    public int getUsosMax() { return usosMax; }
    public String getObservacao() { return observacao; }
    public Instant getCriadoEm() { return criadoEm; }
    public Instant getCanceladoEm() { return canceladoEm; }
}
