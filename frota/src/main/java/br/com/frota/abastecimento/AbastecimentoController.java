package br.com.frota.abastecimento;

import br.com.frota.abastecimento.AbastecimentoDtos.AbastecimentoRequest;
import br.com.frota.abastecimento.AbastecimentoDtos.AbastecimentoResponse;
import br.com.frota.abastecimento.AbastecimentoDtos.AnularRequest;
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
@RequestMapping("/abastecimentos")
public class AbastecimentoController {

    private final AbastecimentoService service;

    public AbastecimentoController(AbastecimentoService service) {
        this.service = service;
    }

    /** Técnico lança nos carros dele; ADMIN e SUPERVISOR em qualquer carro. GERENTE só consulta. */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','TECNICO')")
    public AbastecimentoResponse registrar(@Valid @RequestBody AbastecimentoRequest request,
                                           @AuthenticationPrincipal Jwt jwt) {
        return service.registrar(request, jwt.<Long>getClaim("uid"), jwt.getClaimAsString("perfil"));
    }

    @GetMapping
    public List<AbastecimentoResponse> listar(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "100") int limite,
            @AuthenticationPrincipal Jwt jwt) {
        return service.listar(veiculoId, de, ate, limite, somenteDoTecnico(jwt));
    }

    @GetMapping("/{id}")
    public AbastecimentoResponse buscar(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.buscar(id, somenteDoTecnico(jwt));
    }

    /** Anula um lançamento errado (não apaga: fica marcado, com motivo e quem anulou). */
    @PostMapping("/{id}/anular")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
    public AbastecimentoResponse anular(@PathVariable Long id, @Valid @RequestBody AnularRequest request,
                                        @AuthenticationPrincipal Jwt jwt) {
        return service.anular(id, request.motivo(), jwt.<Long>getClaim("uid"));
    }

    private Long somenteDoTecnico(Jwt jwt) {
        return "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
    }
}
