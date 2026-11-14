package br.com.frota.acompanhamento;

import br.com.frota.acompanhamento.AcompanhamentoDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
public class AcompanhamentoController {

    private static final String LANCAMENTO = "hasAnyRole('ADMIN','SUPERVISOR','TECNICO')";

    private final AcompanhamentoService service;

    public AcompanhamentoController(AcompanhamentoService service) {
        this.service = service;
    }

    // ---------- observações diárias ----------

    @PostMapping("/observacoes")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(LANCAMENTO)
    public ObservacaoResponse registrarObservacao(@Valid @RequestBody ObservacaoRequest request,
                                                  @AuthenticationPrincipal Jwt jwt) {
        return service.registrarObservacao(request, jwt.<Long>getClaim("uid"), jwt.getClaimAsString("perfil"));
    }

    @GetMapping("/observacoes")
    public List<ObservacaoResponse> listarObservacoes(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "100") int limite,
            @AuthenticationPrincipal Jwt jwt) {
        return service.listarObservacoes(veiculoId, de, ate, limite, somenteDoTecnico(jwt));
    }

    // ---------- km da semana ----------

    @PostMapping("/km-semanal")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(LANCAMENTO)
    public KmSemanalResponse registrarKm(@Valid @RequestBody KmSemanalRequest request,
                                         @AuthenticationPrincipal Jwt jwt) {
        return service.registrarKm(request, jwt.<Long>getClaim("uid"), jwt.getClaimAsString("perfil"));
    }

    @GetMapping("/km-semanal")
    public List<KmSemanalResponse> listarKm(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(defaultValue = "100") int limite,
            @AuthenticationPrincipal Jwt jwt) {
        return service.listarKm(veiculoId, de, ate, limite, somenteDoTecnico(jwt));
    }

    private Long somenteDoTecnico(Jwt jwt) {
        return "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
    }
}
