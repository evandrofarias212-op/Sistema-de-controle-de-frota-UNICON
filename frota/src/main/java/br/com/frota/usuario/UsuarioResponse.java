package br.com.frota.usuario;

import java.time.LocalDateTime;

public record UsuarioResponse(
        Long id,
        String nome,
        String email,
        Perfil perfil,
        Equipe equipe,
        boolean ativo,
        LocalDateTime criadoEm) {

    public static UsuarioResponse de(Usuario u) {
        return new UsuarioResponse(u.getId(), u.getNome(), u.getEmail(), u.getPerfil(),
                u.getEquipe(), u.isAtivo(), u.getCriadoEm());
    }
}
