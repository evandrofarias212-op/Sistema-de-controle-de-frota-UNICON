package br.com.frota.vistoria;

import br.com.frota.veiculo.TipoVeiculo;
import jakarta.persistence.*;

@Entity
@Table(name = "itens_vistoria")
public class ItemVistoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 80)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CategoriaItem categoria;

    @Column(nullable = false)
    private boolean obrigatorio = true;

    @Column(nullable = false)
    private boolean ativo = true;

    @Column(nullable = false)
    private int ordem;

    @Enumerated(EnumType.STRING)
    @Column(name = "aplica_a", nullable = false, length = 10)
    private AplicaA aplicaA = AplicaA.AMBOS;

    protected ItemVistoria() {
        // exigido pelo JPA
    }

    public ItemVistoria(String nome, CategoriaItem categoria, boolean obrigatorio, boolean ativo, int ordem) {
        this.nome = nome;
        this.categoria = categoria;
        this.obrigatorio = obrigatorio;
        this.ativo = ativo;
        this.ordem = ordem;
    }

    public void atualizar(String nome, CategoriaItem categoria, boolean obrigatorio, boolean ativo, int ordem) {
        this.nome = nome;
        this.categoria = categoria;
        this.obrigatorio = obrigatorio;
        this.ativo = ativo;
        this.ordem = ordem;
    }

    public Long getId() { return id; }
    public String getNome() { return nome; }
    public CategoriaItem getCategoria() { return categoria; }
    public boolean isObrigatorio() { return obrigatorio; }
    public boolean isAtivo() { return ativo; }
    public int getOrdem() { return ordem; }
    public AplicaA getAplicaA() { return aplicaA; }
    public void setAplicaA(AplicaA aplicaA) { this.aplicaA = aplicaA; }

    /** O item vale para esse tipo de veículo? */
    public boolean aplicaAo(TipoVeiculo tipo) {
        return aplicaA == AplicaA.AMBOS || aplicaA.name().equals(tipo.name());
    }
}
