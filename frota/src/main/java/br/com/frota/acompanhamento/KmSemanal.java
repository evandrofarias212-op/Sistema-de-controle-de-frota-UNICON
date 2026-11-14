package br.com.frota.acompanhamento;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Leitura do odômetro da semana. kmPercorrido = km desta leitura menos o da leitura anterior. */
@Entity
@Table(name = "km_semanais")
public class KmSemanal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private Long veiculoId;

    @Column(name = "lancado_por", nullable = false, updatable = false)
    private Long lancadoPor;

    /** Segunda-feira da semana. */
    @Column(name = "semana_inicio", nullable = false, updatable = false)
    private LocalDate semanaInicio;

    @Column(nullable = false, updatable = false)
    private int km;

    @Column(name = "km_percorrido", updatable = false)
    private Integer kmPercorrido;

    @Column(updatable = false, length = 300)
    private String observacao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected KmSemanal() {
        // exigido pelo JPA
    }

    public KmSemanal(Long veiculoId, Long lancadoPor, LocalDate semanaInicio, int km,
                     Integer kmPercorrido, String observacao) {
        this.veiculoId = veiculoId;
        this.lancadoPor = lancadoPor;
        this.semanaInicio = semanaInicio;
        this.km = km;
        this.kmPercorrido = kmPercorrido;
        this.observacao = observacao;
    }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public Long getLancadoPor() { return lancadoPor; }
    public LocalDate getSemanaInicio() { return semanaInicio; }
    public int getKm() { return km; }
    public Integer getKmPercorrido() { return kmPercorrido; }
    public String getObservacao() { return observacao; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
