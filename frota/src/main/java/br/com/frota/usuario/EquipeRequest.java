package br.com.frota.usuario;

import jakarta.validation.constraints.NotNull;

public record EquipeRequest(@NotNull Equipe equipe) {
}
