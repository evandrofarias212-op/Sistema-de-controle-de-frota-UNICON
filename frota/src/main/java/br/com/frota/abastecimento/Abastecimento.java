package br.com.frota.abastecimento;

import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Um abastecimento. Só nasce com as 3 fotos: vale, bomba e km. Não é editável. */
@Entity
@Table(name = "abastecimentos")
public class Abastecimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private Long veiculoId;

    @Column(name = "lancado_por", nullable = false, updatable = false)
    private Long lancadoPor;

    @Column(name = "abastecido_em", nullable = false, updatable = false)
    private LocalDateTime abastecidoEm;

    @Column(nullable = false, updatable = false)
    private int km;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    private Combustivel combustivel;

    @Column(nullable = false, updatable = false, precision = 8, scale = 2)
    private BigDecimal litros;

    @Column(nullable = false, updatable = false, precision = 10, scale = 2)
    private BigDecimal valor;

    @Column(updatable = false, length = 80)
    private String posto;

    @Column(updatable = false, length = 500)
    private String observacao;

    @Column(name = "foto_vale_id", nullable = false, updatable = false)
    private Long fotoValeId;

    @Column(name = "foto_bomba_id", nullable = false, updatable = false)
    private Long fotoBombaId;

    @Column(name = "foto_km_id", nullable = false, updatable = false)
    private Long fotoKmId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected Abastecimento() {
        // exigido pelo JPA
    }

    public Abastecimento(Long veiculoId, Long lancadoPor, LocalDateTime abastecidoEm, int km,
                         Combustivel combustivel, BigDecimal litros, BigDecimal valor,
                         String posto, String observacao,
                         Long fotoValeId, Long fotoBombaId, Long fotoKmId) {
        this.veiculoId = veiculoId;
        this.lancadoPor = lancadoPor;
        this.abastecidoEm = abastecidoEm;
        this.km = km;
        this.combustivel = combustivel;
        this.litros = litros;
        this.valor = valor;
        this.posto = posto;
        this.observacao = observacao;
        this.fotoValeId = fotoValeId;
        this.fotoBombaId = fotoBombaId;
        this.fotoKmId = fotoKmId;
    }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public Long getLancadoPor() { return lancadoPor; }
    public LocalDateTime getAbastecidoEm() { return abastecidoEm; }
    public int getKm() { return km; }
    public Combustivel getCombustivel() { return combustivel; }
    public BigDecimal getLitros() { return litros; }
    public BigDecimal getValor() { return valor; }
    public String getPosto() { return posto; }
    public String getObservacao() { return observacao; }
    public Long getFotoValeId() { return fotoValeId; }
    public Long getFotoBombaId() { return fotoBombaId; }
    public Long getFotoKmId() { return fotoKmId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
