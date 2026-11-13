package br.com.frota.auth;

import br.com.frota.usuario.Perfil;
import java.time.Instant;

public record LoginResponse(
        String token,
        String tipo,
        Instant expiraEm,
        String nome,
        Perfil perfil) {
}
