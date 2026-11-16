package br.com.frota.manutencao;

import br.com.frota.manutencao.ManutencaoDtos.AnularManutencaoRequest;
import br.com.frota.manutencao.ManutencaoDtos.ManutencaoRequest;
import br.com.frota.manutencao.ManutencaoDtos.ManutencaoResponse;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/manutencoes")
public class ManutencaoController {

    private final ManutencaoService service;

    public ManutencaoController(ManutencaoService service) {
        this.service = service;
    }

    /** Técnico: só troca de óleo, nos carros dele, com foto do comprovante. ADMIN e SUPERVISOR: qualquer tipo. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','TECNICO')")
    public ManutencaoResponse registrar(@Valid @RequestBody ManutencaoRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return service.registrar(request, jwt.<Long>getClaim("uid"), jwt.getClaimAsString("perfil"));
    }

    @GetMapping
    public List<ManutencaoResponse> listar(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) TipoManutencao tipo,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "100") int limite,
            @AuthenticationPrincipal Jwt jwt) {
        return service.listar(veiculoId, tipo, de, ate, limite, somenteDoTecnico(jwt));
    }

    @GetMapping("/{id}")
    public ManutencaoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, somenteDoTecnico(jwt));
    }

    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public ManutencaoResponse anular(@PathVariable Long id, @Valid @RequestBody AnularManutencaoRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.anular(id, request.motivo(), jwt.<Long>getClaim("uid"));
    }

    private Long somenteDoTecnico(Jwt jwt) {
        return "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
    }
}
