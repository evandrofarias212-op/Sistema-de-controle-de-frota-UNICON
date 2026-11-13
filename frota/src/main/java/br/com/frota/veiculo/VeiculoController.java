package br.com.frota.veiculo;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/veiculos")
public class VeiculoController {

    private final VeiculoService service;

    public VeiculoController(VeiculoService service) {
        this.service = service;
    }

    /** ADMIN, SUPERVISOR e GERENTE veem todos; TECNICO vê só os carros dele. */
    @GetMapping
    public List<VeiculoResponse> listar(@AuthenticationPrincipal Jwt jwt) {
        return service.listar(somenteDoTecnico(jwt));
    }

    @GetMapping("/{id}")
    public VeiculoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, somenteDoTecnico(jwt));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public VeiculoResponse criar(@Valid @RequestBody VeiculoRequest request) {
        return service.criar(request);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public VeiculoResponse atualizar(@PathVariable Long id, @Valid @RequestBody VeiculoRequest request) {
        return service.atualizar(id, request);
    }

    /** Devolve o id do técnico logado (pra filtrar) ou null para os demais perfis. */
    private Long somenteDoTecnico(Jwt jwt) {
        return "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
    }
}
