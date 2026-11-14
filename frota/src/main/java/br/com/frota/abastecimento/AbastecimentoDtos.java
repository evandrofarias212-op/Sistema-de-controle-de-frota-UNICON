package br.com.frota.abastecimento;

import br.com.frota.usuario.Equipe;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class AbastecimentoDtos {

    private AbastecimentoDtos() {
    }

    // ---------- entrada ----------

    public record AbastecimentoRequest(
            @NotNull Long veiculoId,
            @NotNull LocalDateTime abastecidoEm,
            @NotNull @Min(0) Integer km,
            @NotNull Combustivel combustivel,
            @NotNull @DecimalMin("0.01") @Digits(integer = 6, fraction = 2) BigDecimal litros,
            @NotNull @DecimalMin("0.01") @Digits(integer = 8, fraction = 2) BigDecimal valor,
            @Size(max = 80) String posto,
            @Size(max = 500) String observacao,
            @NotNull Long fotoValeId,
            @NotNull Long fotoBombaId,
            @NotNull Long fotoKmId) {
    }

    // ---------- saída ----------

    public record AbastecimentoResponse(
            Long id,
            Long veiculoId,
            String placa,
            Long lancadoPor,
            String lancadoPorNome,
            LocalDateTime abastecidoEm,
            int km,
            Combustivel combustivel,
            BigDecimal litros,
            BigDecimal valor,
            BigDecimal precoPorLitro,
            String posto,
            String observacao,
            Long fotoValeId,
            Long fotoBombaId,
            Long fotoKmId,
            LocalDateTime criadoEm) {

        public static AbastecimentoResponse de(Abastecimento a, String placa, String lancadoPorNome) {
            return new AbastecimentoResponse(a.getId(), a.getVeiculoId(), placa, a.getLancadoPor(), lancadoPorNome,
                    a.getAbastecidoEm(), a.getKm(), a.getCombustivel(), a.getLitros(), a.getValor(),
                    a.getValor().divide(a.getLitros(), 3, RoundingMode.HALF_UP),
                    a.getPosto(), a.getObservacao(),
                    a.getFotoValeId(), a.getFotoBombaId(), a.getFotoKmId(), a.getCriadoEm());
        }
    }

    public record LinhaVeiculo(Long veiculoId, String placa, Equipe equipe,
                               long abastecimentos, BigDecimal litros, BigDecimal valor) {
    }

    public record LinhaEquipe(Equipe equipe, long abastecimentos, BigDecimal litros, BigDecimal valor) {
    }

    public record RelatorioCombustivel(LocalDate de, LocalDate ate,
                                       long totalAbastecimentos, BigDecimal totalLitros, BigDecimal totalValor,
                                       List<LinhaEquipe> porEquipe, List<LinhaVeiculo> porVeiculo) {
    }
}
