package br.com.frota.vistoria;

import br.com.frota.veiculo.Veiculo;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "vistorias")
public class Vistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "veiculo_id", nullable = false)
    private Veiculo veiculo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoVistoria tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVistoria status = StatusVistoria.AGENDADA;

    @Column(name = "data_agendada", nullable = false)
    private LocalDate dataAgendada;

    @Column(name = "realizada_em")
    private LocalDateTime realizadaEm;

    @Column(name = "realizada_por")
    private Long realizadaPor;

    private Integer km;

    @Column(length = 1000)
    private String observacoes;

    @Column(name = "criado_por", nullable = false, updatable = false)
    private Long criadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected Vistoria() {
        // exigido pelo JPA
    }

    public Vistoria(Veiculo veiculo, TipoVistoria tipo, LocalDate dataAgendada, Long criadoPor) {
        this.veiculo = veiculo;
        this.tipo = tipo;
        this.dataAgendada = dataAgendada;
        this.criadoPor = criadoPor;
    }

    public void concluir(int km, String observacoes, Long usuarioId) {
        this.status = StatusVistoria.REALIZADA;
        this.km = km;
        this.observacoes = observacoes;
        this.realizadaEm = LocalDateTime.now();
        this.realizadaPor = usuarioId;
    }

    public void cancelar() {
        this.status = StatusVistoria.CANCELADA;
    }

    public void setDataAgendada(LocalDate dataAgendada) { this.dataAgendada = dataAgendada; }

    public Long getId() { return id; }
    public Veiculo getVeiculo() { return veiculo; }
    public TipoVistoria getTipo() { return tipo; }
    public StatusVistoria getStatus() { return status; }
    public LocalDate getDataAgendada() { return dataAgendada; }
    public LocalDateTime getRealizadaEm() { return realizadaEm; }
    public Long getRealizadaPor() { return realizadaPor; }
    public Integer getKm() { return km; }
    public String getObservacoes() { return observacoes; }
    public Long getCriadoPor() { return criadoPor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
