package br.com.frota.usuario;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/usuarios")
@PreAuthorize("hasRole('ADMIN')")
public class UsuarioController {

    private final UsuarioService service;

    public UsuarioController(UsuarioService service) {
        this.service = service;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioResponse criar(@Valid @RequestBody UsuarioRequest request) {
        return UsuarioResponse.de(service.criar(request));
    }

    @GetMapping
    public List<UsuarioResponse> listar() {
        return service.listar().stream().map(UsuarioResponse::de).toList();
    }

    @PatchMapping("/{id}/ativo")
    public UsuarioResponse alterarAtivo(@PathVariable Long id,
                                        @Valid @RequestBody AtivoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        Long meuId = jwt.getClaim("uid");
        if (id.equals(meuId) && !request.ativo()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Você não pode desativar a própria conta");
        }
        return UsuarioResponse.de(service.alterarAtivo(id, request.ativo()));
    }

    @PatchMapping("/{id}/equipe")
    public UsuarioResponse alterarEquipe(@PathVariable Long id,
                                         @Valid @RequestBody EquipeRequest request) {
        return UsuarioResponse.de(service.alterarEquipe(id, request.equipe()));
    }
}
