package br.com.frota.veiculo;

import br.com.frota.usuario.Equipe;
import br.com.frota.usuario.Usuario;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "veiculos")
public class Veiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 8)
    private String placa;

    @Column(nullable = false, length = 60)
    private String marca;

    @Column(nullable = false, length = 80)
    private String modelo;

    @Column(nullable = false)
    private int ano;

    @Column(name = "km_atual", nullable = false)
    private int kmAtual;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private StatusVeiculo status = StatusVeiculo.ATIVO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Equipe equipe;

    /** 1 ou 2 técnicos (dupla). */
    @ManyToMany
    @JoinTable(name = "veiculo_responsaveis",
            joinColumns = @JoinColumn(name = "veiculo_id"),
            inverseJoinColumns = @JoinColumn(name = "usuario_id"))
    private Set<Usuario> responsaveis = new HashSet<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected Veiculo() {
        // exigido pelo JPA
    }

    public Veiculo(String placa, String marca, String modelo, int ano, int kmAtual,
                   StatusVeiculo status, Equipe equipe) {
        this.placa = placa;
        this.marca = marca;
        this.modelo = modelo;
        this.ano = ano;
        this.kmAtual = kmAtual;
        this.status = status;
        this.equipe = equipe;
    }

    public void trocarResponsaveis(Set<Usuario> novos) {
        this.responsaveis.clear();
        this.responsaveis.addAll(novos);
    }

    public Long getId() { return id; }
    public String getPlaca() { return placa; }
    public String getMarca() { return marca; }
    public String getModelo() { return modelo; }
    public int getAno() { return ano; }
    public int getKmAtual() { return kmAtual; }
    public StatusVeiculo getStatus() { return status; }
    public Equipe getEquipe() { return equipe; }
    public Set<Usuario> getResponsaveis() { return responsaveis; }
    public LocalDateTime getCriadoEm() { return criadoEm; }

    public void setPlaca(String placa) { this.placa = placa; }
    public void setMarca(String marca) { this.marca = marca; }
    public void setModelo(String modelo) { this.modelo = modelo; }
    public void setAno(int ano) { this.ano = ano; }
    public void setKmAtual(int kmAtual) { this.kmAtual = kmAtual; }
    public void setStatus(StatusVeiculo status) { this.status = status; }
    public void setEquipe(Equipe equipe) { this.equipe = equipe; }
}
