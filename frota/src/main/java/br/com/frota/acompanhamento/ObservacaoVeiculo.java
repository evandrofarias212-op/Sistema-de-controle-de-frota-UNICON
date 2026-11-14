package br.com.frota.acompanhamento;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

/** Observação do dia a dia sobre um carro. Não é editável. */
@Entity
@Table(name = "observacoes_veiculo")
public class ObservacaoVeiculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "veiculo_id", nullable = false, updatable = false)
    private Long veiculoId;

    @Column(name = "autor_id", nullable = false, updatable = false)
    private Long autorId;

    @Column(nullable = false, updatable = false)
    private LocalDate dia;

    @Column(nullable = false, updatable = false, length = 1000)
    private String texto;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void aoCriar() {
        this.criadoEm = LocalDateTime.now();
    }

    protected ObservacaoVeiculo() {
        // exigido pelo JPA
    }

    public ObservacaoVeiculo(Long veiculoId, Long autorId, LocalDate dia, String texto) {
        this.veiculoId = veiculoId;
        this.autorId = autorId;
        this.dia = dia;
        this.texto = texto;
    }

    public Long getId() { return id; }
    public Long getVeiculoId() { return veiculoId; }
    public Long getAutorId() { return autorId; }
    public LocalDate getDia() { return dia; }
    public String getTexto() { return texto; }
    public LocalDateTime getCriadoEm() { return criadoEm; }
}
