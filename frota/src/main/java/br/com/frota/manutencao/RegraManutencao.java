package br.com.frota.manutencao;

import br.com.frota.veiculo.TipoVeiculo;
import jakarta.persistence.*;

/** Parâmetro padrão de manutenção preventiva para um tipo de veículo (ex.: CARRO + TROCA_CORREIA). */
@Entity
@Table(name = "regras_manutencao")
public class RegraManutencao {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_veiculo", nullable = false, updatable = false, length = 20)
    private TipoVeiculo tipoVeiculo;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_manutencao", nullable = false, updatable = false, length = 20)
    private TipoManutencao tipoManutencao;

    @Column(name = "intervalo_km")
    private Integer intervaloKm;

    @Column(name = "intervalo_meses")
    private Integer intervaloMeses;

    @Column(name = "alerta_km", nullable = false)
    private int alertaKm = 1000;

    @Column(name = "alerta_dias", nullable = false)
    private int alertaDias = 15;

    @Column(nullable = false)
    private boolean ativo = true;

    protected RegraManutencao() {
        // exigido pelo JPA
    }

    public RegraManutencao(TipoVeiculo tipoVeiculo, TipoManutencao tipoManutencao) {
        this.tipoVeiculo = tipoVeiculo;
        this.tipoManutencao = tipoManutencao;
    }

    public void definir(Integer intervaloKm, Integer intervaloMeses, int alertaKm, int alertaDias, boolean ativo) {
        this.intervaloKm = intervaloKm;
        this.intervaloMeses = intervaloMeses;
        this.alertaKm = alertaKm;
        this.alertaDias = alertaDias;
        this.ativo = ativo;
    }

    public Long getId() { return id; }
    public TipoVeiculo getTipoVeiculo() { return tipoVeiculo; }
    public TipoManutencao getTipoManutencao() { return tipoManutencao; }
    public Integer getIntervaloKm() { return intervaloKm; }
    public Integer getIntervaloMeses() { return intervaloMeses; }
    public int getAlertaKm() { return alertaKm; }
    public int getAlertaDias() { return alertaDias; }
    public boolean isAtivo() { return ativo; }
}
