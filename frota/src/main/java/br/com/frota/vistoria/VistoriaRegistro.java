package br.com.frota.vistoria;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/** O que foi constatado em um item numa vistoria: estado, observação e fotos. */
@Entity
@Table(name = "vistoria_registros")
public class VistoriaRegistro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vistoria_id", nullable = false, updatable = false)
    private Long vistoriaId;

    @Column(name = "item_id", nullable = false, updatable = false)
    private Long itemId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoItem estado;

    @Column(length = 500)
    private String observacao;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vistoria_registro_fotos", joinColumns = @JoinColumn(name = "registro_id"))
    @Column(name = "arquivo_id", nullable = false)
    private Set<Long> arquivoIds = new HashSet<>();

    protected VistoriaRegistro() {
        // exigido pelo JPA
    }

    public VistoriaRegistro(Long vistoriaId, Long itemId) {
        this.vistoriaId = vistoriaId;
        this.itemId = itemId;
    }

    public void atualizar(EstadoItem estado, String observacao, Set<Long> arquivoIds) {
        this.estado = estado;
        this.observacao = observacao;
        this.arquivoIds.clear();
        this.arquivoIds.addAll(arquivoIds);
    }

    public Long getId() { return id; }
    public Long getVistoriaId() { return vistoriaId; }
    public Long getItemId() { return itemId; }
    public EstadoItem getEstado() { return estado; }
    public String getObservacao() { return observacao; }
    public Set<Long> getArquivoIds() { return arquivoIds; }
}
