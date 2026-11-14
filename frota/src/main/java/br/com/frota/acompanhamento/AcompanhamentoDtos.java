package br.com.frota.acompanhamento;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class AcompanhamentoDtos {

    private AcompanhamentoDtos() {
    }

    // ---------- observações ----------

    /** data é opcional: se não vier, vale hoje. */
    public record ObservacaoRequest(
            @NotNull Long veiculoId,
            LocalDate data,
            @NotBlank @Size(max = 1000) String texto) {
    }

    public record ObservacaoResponse(Long id, Long veiculoId, String placa, Long autorId, String autorNome,
                                     LocalDate data, String texto, LocalDateTime criadoEm) {
        public static ObservacaoResponse de(ObservacaoVeiculo o, String placa, String autorNome) {
            return new ObservacaoResponse(o.getId(), o.getVeiculoId(), placa, o.getAutorId(), autorNome,
                    o.getDia(), o.getTexto(), o.getCriadoEm());
        }
    }

    // ---------- km da semana ----------

    /** data é opcional (qualquer dia da semana); se não vier, vale hoje. km = leitura do odômetro. */
    public record KmSemanalRequest(
            @NotNull Long veiculoId,
            LocalDate data,
            @NotNull @Min(0) Integer km,
            @Size(max = 300) String observacao) {
    }

    public record KmSemanalResponse(Long id, Long veiculoId, String placa, Long lancadoPor, String lancadoPorNome,
                                    LocalDate semanaInicio, LocalDate semanaFim, int km, Integer kmPercorrido,
                                    String observacao, LocalDateTime criadoEm) {
        public static KmSemanalResponse de(KmSemanal k, String placa, String lancadoPorNome) {
            return new KmSemanalResponse(k.getId(), k.getVeiculoId(), placa, k.getLancadoPor(), lancadoPorNome,
                    k.getSemanaInicio(), k.getSemanaInicio().plusDays(6), k.getKm(), k.getKmPercorrido(),
                    k.getObservacao(), k.getCriadoEm());
        }
    }
}
