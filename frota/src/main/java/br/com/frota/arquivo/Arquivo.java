package br.com.frota.arquivo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "arquivos")
public class Arquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nome_original", nullable = false)
    private String nomeOriginal;

    @Column(name = "tipo_conteudo", nullable = false, length = 100)
    private String tipoConteudo;

    @Column(name = "tamanho_bytes", nullable = false)
    private long tamanhoBytes;

    @Column(nullable = false, length = 64)
    private String sha256;

    /** Chave do arquivo no armazenamento (nunca vem do cliente). */
    @Column(nullable = false)
    private String caminho;

    @Column(nullable = false)
    private boolean sensivel;

    @Column(name = "enviado_por", nullable = false)
    private Long enviadoPor;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected Arquivo() {
        // exigido pelo JPA
    }

    public Arquivo(String nomeOriginal, String tipoConteudo, long tamanhoBytes, String sha256,
                   String caminho, boolean sensivel, Long enviadoPor) {
        this.nomeOriginal = nomeOriginal;
        this.tipoConteudo = tipoConteudo;
        this.tamanhoBytes = tamanhoBytes;
        this.sha256 = sha256;
        this.caminho = caminho;
        this.sensivel = sensivel;
        this.enviadoPor = enviadoPor;
    }

    public Long getId() { return id; }
    public String getNomeOriginal() { return nomeOriginal; }
    public String getTipoConteudo() { return tipoConteudo; }
    public long getTamanhoBytes() { return tamanhoBytes; }
    public String getSha256() { return sha256; }
    public String getCaminho() { return caminho; }
    public boolean isSensivel() { return sensivel; }
    public Long getEnviadoPor() { return enviadoPor; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
