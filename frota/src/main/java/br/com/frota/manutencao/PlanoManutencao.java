package br.com.frota.manutencao;

import jakarta.persistence.*;

/** Intervalo de manutenção preventiva de UM veículo (sobrepõe a regra padrão do tipo de veículo). */
@Entity
@Table(name = "planos_manutencao")
public class PlanoManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private Long veiculoId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_manutencao", nullable = false, updatable = false, length = 20)
    private TipoManutencao tipoManutencao;

    @Column(name = "intervalo_km")
    private Integer intervaloKm;

    @Column(name = "intervalo_meses")
    private Integer intervaloMeses;

    protected PlanoManutencao() {
        // exigido pelo JPA
    }

    public PlanoManutencao(Long veiculoId, TipoManutencao tipoManutencao) {
        this.veiculoId = veiculoId;
        this.tipoManutencao = tipoManutencao;
    }

    public void definir(Integer intervaloKm, Integer intervaloMeses) {
        this.intervaloKm = intervaloKm;
        this.intervaloMeses = intervaloMeses;
    }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public TipoManutencao getTipoManutencao() { return tipoManutencao; }
    public Integer getIntervaloKm() { return intervaloKm; }
    public Integer getIntervaloMeses() { return intervaloMeses; }
}
