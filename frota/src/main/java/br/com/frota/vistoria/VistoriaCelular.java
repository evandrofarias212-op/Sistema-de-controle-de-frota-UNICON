package br.com.frota.vistoria;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/** Resultado da vistoria do celular de um técnico. A senha em si NUNCA é guardada aqui. */
@Entity
@Table(name = "vistoria_celulares")
public class VistoriaCelular {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vistoria_id", nullable = false, updatable = false)
    private Long vistoriaId;

    @Column(name = "tecnico_id", nullable = false, updatable = false)
    private Long tecnicoId;

    @Column(length = 80)
    private String aparelho;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAcessorio capa;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SituacaoAcessorio pelicula;

    @Column(name = "senha_conferida", nullable = false)
    private boolean senhaConferida;

    @Column(name = "senha_trocada", nullable = false)
    private boolean senhaTrocada;

    @Column(length = 500)
    private String observacao;

    /** Arquivo SENSÍVEL (criptografado, só ADMIN abre). */
    @Column(name = "video_senha_arquivo_id")
    private Long videoSenhaArquivoId;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "vistoria_celular_fotos", joinColumns = @JoinColumn(name = "celular_id"))
    @Column(name = "arquivo_id", nullable = false)
    private Set<Long> fotoArquivoIds = new HashSet<>();

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected VistoriaCelular() {
        // exigido pelo JPA
    }

    public VistoriaCelular(Long vistoriaId, Long tecnicoId) {
        this.vistoriaId = vistoriaId;
        this.tecnicoId = tecnicoId;
    }

    public void atualizar(String aparelho, SituacaoAcessorio capa, SituacaoAcessorio pelicula,
                          boolean senhaConferida, boolean senhaTrocada, String observacao,
                          Set<Long> fotoArquivoIds) {
        this.aparelho = aparelho;
        this.capa = capa;
        this.pelicula = pelicula;
        this.senhaConferida = senhaConferida;
        this.senhaTrocada = senhaTrocada;
        this.observacao = observacao;
        this.fotoArquivoIds.clear();
        this.fotoArquivoIds.addAll(fotoArquivoIds);
    }

    public void setVideoSenhaArquivoId(Long videoSenhaArquivoId) { this.videoSenhaArquivoId = videoSenhaArquivoId; }

    public Long getId() { return id; }
    public Long getVistoriaId() { return vistoriaId; }
    public Long getTecnicoId() { return tecnicoId; }
    public String getAparelho() { return aparelho; }
    public SituacaoAcessorio getCapa() { return capa; }
    public SituacaoAcessorio getPelicula() { return pelicula; }
    public boolean isSenhaConferida() { return senhaConferida; }
    public boolean isSenhaTrocada() { return senhaTrocada; }
    public String getObservacao() { return observacao; }
    public Long getVideoSenhaArquivoId() { return videoSenhaArquivoId; }
    public Set<Long> getFotoArquivoIds() { return fotoArquivoIds; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
