package br.com.frota.manutencao;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/** Uma manutenção feita no carro. Não é editável; se estiver errada, é anulada com motivo. */
@Entity
@Table(name = "manutencoes")
public class Manutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private Long veiculoId;

    @Column(name = "lancado_por", nullable = false, updatable = false)
    private Long lancadoPor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private TipoManutencao tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private CategoriaManutencao categoria;

    @Column(name = "realizada_em", nullable = false, updatable = false)
    private LocalDate realizadaEm;

    @Column(nullable = false, updatable = false)
    private int km;

    @Column(updatable = false, length = 100)
    private String oficina;

    @Column(updatable = false, length = 500)
    private String descricao;

    @Column(updatable = false, precision = 10, scale = 2)
    private BigDecimal custo;

    @Column(name = "comprovante_id", updatable = false)
    private Long comprovanteId;

    @Column(name = "anulado_em")
    private LocalDateTime anuladoEm;

    @Column(name = "anulado_por")
    private Long anuladoPor;

    @Column(name = "motivo_anulacao", length = 300)
    private String motivoAnulacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected Manutencao() {
        // exigido pelo JPA
    }

    public Manutencao(Long veiculoId, Long lancadoPor, TipoManutencao tipo, CategoriaManutencao categoria,
                      LocalDate realizadaEm, int km, String oficina, String descricao,
                      BigDecimal custo, Long comprovanteId) {
        this.veiculoId = veiculoId;
        this.lancadoPor = lancadoPor;
        this.tipo = tipo;
        this.categoria = categoria;
        this.realizadaEm = realizadaEm;
        this.km = km;
        this.oficina = oficina;
        this.descricao = descricao;
        this.custo = custo;
        this.comprovanteId = comprovanteId;
    }

    public void anular(Long usuarioId, String motivo) {
        this.anuladoEm = LocalDateTime.now();
        this.anuladoPor = usuarioId;
        this.motivoAnulacao = motivo;
    }

    public boolean isAnulada() { return anuladoEm != null; }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public Long getLancadoPor() { return lancadoPor; }
    public TipoManutencao getTipo() { return tipo; }
    public CategoriaManutencao getCategoria() { return categoria; }
    public LocalDate getRealizadaEm() { return realizadaEm; }
    public int getKm() { return km; }
    public String getOficina() { return oficina; }
    public String getDescricao() { return descricao; }
    public BigDecimal getCusto() { return custo; }
    public Long getComprovanteId() { return comprovanteId; }
    public LocalDateTime getAnuladoEm() { return anuladoEm; }
    public Long getAnuladoPor() { return anuladoPor; }
    public String getMotivoAnulacao() { return motivoAnulacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
