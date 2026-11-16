package br.com.frota.manutencao;

import br.com.frota.usuario.Equipe;
import br.com.frota.veiculo.TipoVeiculo;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ManutencaoDtos {

    private ManutencaoDtos() {
    }

    // ---------- manutenção ----------

    /** realizadaEm opcional (vale hoje). categoria opcional (preventivas = PREVENTIVA; o resto = CORRETIVA). */
    public record ManutencaoRequest(
            @NotNull Long veiculoId,
            @NotNull TipoManutencao tipo,
            CategoriaManutencao categoria,
            LocalDate realizadaEm,
            @NotNull @Min(0) Integer km,
            @Size(max = 100) String oficina,
            @Size(max = 500) String descricao,
            @DecimalMin("0.00") @Digits(integer = 8, fraction = 2) BigDecimal custo,
            Long comprovanteId) {
    }

    public record AnularManutencaoRequest(@NotBlank @Size(min = 5, max = 300) String motivo) {
    }

    public record ManutencaoResponse(Long id, Long veiculoId, String placa, Long lancadoPor, String lancadoPorNome,
                                     TipoManutencao tipo, CategoriaManutencao categoria, LocalDate realizadaEm,
                                     int km, String oficina, String descricao, BigDecimal custo, Long comprovanteId,
                                     LocalDateTime criadoEm, boolean anulada, LocalDateTime anuladoEm,
                                     String motivoAnulacao) {
        public static ManutencaoResponse de(Manutencao m, String placa, String lancadoPorNome) {
            return new ManutencaoResponse(m.getId(), m.getVeiculoId(), placa, m.getLancadoPor(), lancadoPorNome,
                    m.getTipo(), m.getCategoria(), m.getRealizadaEm(), m.getKm(), m.getOficina(), m.getDescricao(),
                    m.getCusto(), m.getComprovanteId(), m.getCriadoEm(), m.isAnulada(), m.getAnuladoEm(),
                    m.getMotivoAnulacao());
        }
    }

    // ---------- manutenção preventiva (óleo, correia...) ----------

    /** Parâmetro padrão para um tipo de veículo. Pelo menos um intervalo (km ou meses) é obrigatório quando ativo. */
    public record RegraRequest(
            @NotNull TipoVeiculo tipoVeiculo,
            @NotNull TipoManutencao tipoManutencao,
            @Min(100) @Max(500000) Integer intervaloKm,
            @Min(1) @Max(120) Integer intervaloMeses,
            @Min(0) @Max(50000) Integer alertaKm,
            @Min(0) @Max(365) Integer alertaDias,
            @NotNull Boolean ativo) {
    }

    public record RegraResponse(Long id, TipoVeiculo tipoVeiculo, TipoManutencao tipoManutencao,
                                Integer intervaloKm, Integer intervaloMeses, int alertaKm, int alertaDias,
                                boolean ativo) {
        public static RegraResponse de(RegraManutencao r) {
            return new RegraResponse(r.getId(), r.getTipoVeiculo(), r.getTipoManutencao(), r.getIntervaloKm(),
                    r.getIntervaloMeses(), r.getAlertaKm(), r.getAlertaDias(), r.isAtivo());
        }
    }

    /** Intervalo de um veículo específico (sobrepõe a regra padrão). */
    public record PlanoRequest(
            @Min(100) @Max(500000) Integer intervaloKm,
            @Min(1) @Max(120) Integer intervaloMeses) {
    }

    public record PlanoResponse(Long veiculoId, TipoManutencao tipoManutencao,
                                Integer intervaloKm, Integer intervaloMeses) {
        public static PlanoResponse de(PlanoManutencao p) {
            return new PlanoResponse(p.getVeiculoId(), p.getTipoManutencao(), p.getIntervaloKm(), p.getIntervaloMeses());
        }
    }

    public record SituacaoPreventiva(Long veiculoId, String placa, TipoVeiculo tipoVeiculo, Equipe equipe,
                                     TipoManutencao tipoManutencao, StatusPreventiva situacao,
                                     LocalDate ultimaData, Integer ultimoKm,
                                     Integer proximaKm, LocalDate proximaData,
                                     Integer kmRestantes, Long diasRestantes,
                                     Integer intervaloKm, Integer intervaloMeses, boolean intervaloPersonalizado) {
    }

    // ---------- gastos ----------

    public record GastoVeiculo(Long veiculoId, String placa, Equipe equipe,
                               BigDecimal combustivel, BigDecimal manutencao, BigDecimal total) {
    }

    public record GastoEquipe(Equipe equipe, BigDecimal combustivel, BigDecimal manutencao, BigDecimal total) {
    }

    public record RelatorioGastos(LocalDate de, LocalDate ate,
                                  BigDecimal totalCombustivel, BigDecimal totalManutencao, BigDecimal totalGeral,
                                  List<GastoEquipe> porEquipe, List<GastoVeiculo> porVeiculo) {
    }
}
