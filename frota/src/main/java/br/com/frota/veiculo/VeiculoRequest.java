package br.com.frota.veiculo;

import br.com.frota.usuario.Equipe;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record VeiculoRequest(
        @NotBlank @Size(max = 10) String placa,
        @NotBlank @Size(max = 60) String marca,
        @NotBlank @Size(max = 80) String modelo,
        @NotNull @Min(1980) @Max(2100) Integer ano,
        @NotNull @Min(0) Integer kmAtual,
        @NotNull StatusVeiculo status,
        @NotNull Equipe equipe,
        TipoVeiculo tipo,
        @Size(max = 2) List<Long> responsaveisIds) {
}
