package br.com.frota.arquivo;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "acessos_arquivo")
public class AcessoArquivo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "arquivo_id", nullable = false)
    private Long arquivoId;

    @Column(name = "usuario_id", nullable = false)
    private Long usuarioId;

    @Column(name = "acessado_em", nullable = false, updatable = false)
    private LocalDateTime acessadoEm;

    @PrePersist
    void aoCriar() {
        this.acessadoEm = LocalDateTime.now();
    }

    protected AcessoArquivo() {
        // exigido pelo JPA
    }

    public AcessoArquivo(Long arquivoId, Long usuarioId) {
        this.arquivoId = arquivoId;
        this.usuarioId = usuarioId;
    }

    public Long getId() { return id; }
    public Long getArquivoId() { return arquivoId; }
    public Long getUsuarioId() { return usuarioId; }
    public LocalDateTime getAcessadoEm() { return acessadoEm; }
}
