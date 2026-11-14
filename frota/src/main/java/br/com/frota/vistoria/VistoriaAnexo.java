package br.com.frota.vistoria;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "vistoria_anexos")
public class VistoriaAnexo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vistoria_id", nullable = false, updatable = false)
    private Long vistoriaId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoAnexo tipo;

    @Column(name = "arquivo_id", nullable = false, updatable = false)
    private Long arquivoId;

    /** Preenchido no termo de responsabilidade: qual técnico assinou. */
    @Column(name = "tecnico_id")
    private Long tecnicoId;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected VistoriaAnexo() {
        // exigido pelo JPA
    }

    public VistoriaAnexo(Long vistoriaId, TipoAnexo tipo, Long arquivoId, Long tecnicoId) {
        this.vistoriaId = vistoriaId;
        this.tipo = tipo;
        this.arquivoId = arquivoId;
        this.tecnicoId = tecnicoId;
    }

    public Long getId() { return id; }
    public Long getVistoriaId() { return vistoriaId; }
    public TipoAnexo getTipo() { return tipo; }
    public Long getArquivoId() { return arquivoId; }
    public Long getTecnicoId() { return tecnicoId; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
