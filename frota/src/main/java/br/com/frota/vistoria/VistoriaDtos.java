package br.com.frota.vistoria;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Todos os formatos de entrada/saída das vistorias num lugar só. */
public final class VistoriaDtos {

    private VistoriaDtos() {
    }

    // ---------- entrada ----------

    public record AgendarRequest(
            @NotNull Long veiculoId,
            @NotNull TipoVistoria tipo,
            @NotNull LocalDate dataAgendada) {
    }

    public record ReagendarRequest(@NotNull LocalDate dataAgendada) {
    }

    public record RegistroRequest(
            @NotNull EstadoItem estado,
            @Size(max = 500) String observacao,
            @Size(max = 30) List<Long> arquivoIds) {
    }

    public record AnexoRequest(
            @NotNull TipoAnexo tipo,
            @NotNull Long arquivoId,
            Long tecnicoId) {
    }

    public record ConcluirRequest(
            @NotNull @Min(0) Integer km,
            @Size(max = 1000) String observacoes) {
    }

    public record ItemRequest(
            @NotBlank @Size(max = 80) String nome,
            @NotNull CategoriaItem categoria,
            @NotNull Boolean obrigatorio,
            @NotNull Boolean ativo,
            @NotNull Integer ordem,
            AplicaA aplicaA) {
    }

    // ---------- saída ----------

    public record ItemResponse(Long id, String nome, CategoriaItem categoria,
                               boolean obrigatorio, boolean ativo, int ordem, AplicaA aplicaA) {
        public static ItemResponse de(ItemVistoria i) {
            return new ItemResponse(i.getId(), i.getNome(), i.getCategoria(), i.isObrigatorio(), i.isAtivo(), i.getOrdem(), i.getAplicaA());
        }
    }

    public record VistoriaResumo(Long id, Long veiculoId, String placa, String modelo,
                                 TipoVistoria tipo, StatusVistoria status,
                                 LocalDate dataAgendada, LocalDateTime realizadaEm) {
    }

    public record VeiculoResumo(Long id, String placa, String modelo) {
    }

    public record ItemDaVistoria(Long itemId, String nome, CategoriaItem categoria, boolean obrigatorio,
                                 EstadoItem estado, String observacao, List<Long> arquivoIds) {
    }

    public record AnexoResponse(Long id, TipoAnexo tipo, Long arquivoId, Long tecnicoId) {
    }

    public record VistoriaDetalhe(Long id, VeiculoResumo veiculo, TipoVistoria tipo, StatusVistoria status,
                                  LocalDate dataAgendada, LocalDateTime realizadaEm, Long realizadaPor,
                                  Integer km, String observacoes,
                                  List<ItemDaVistoria> itens, List<AnexoResponse> anexos,
                                  List<String> pendencias) {
    }
}
