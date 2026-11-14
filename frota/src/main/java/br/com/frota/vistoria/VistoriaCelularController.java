package br.com.frota.vistoria;

import br.com.frota.vistoria.CelularDtos.CelularRequest;
import br.com.frota.vistoria.CelularDtos.CelularResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/vistorias/{vistoriaId}/celulares")
public class VistoriaCelularController {

    private static final String ESCRITA = "hasAnyRole('ADMIN','SUPERVISOR')";
    private static final String LEITURA = "hasAnyRole('ADMIN','SUPERVISOR','GERENTE')";

    private final VistoriaCelularService service;

    public VistoriaCelularController(VistoriaCelularService service) {
        this.service = service;
    }

    @GetMapping
    @PreAuthorize(LEITURA)
    public List<CelularResponse> listar(@PathVariable Long vistoriaId, @AuthenticationPrincipal Jwt jwt) {
        return service.listar(vistoriaId, ehAdmin(jwt));
    }

    /** Cria ou atualiza o registro do celular desse técnico nessa vistoria. */
    @PutMapping("/{tecnicoId}")
    @PreAuthorize(ESCRITA)
    public CelularResponse registrar(@PathVariable Long vistoriaId, @PathVariable Long tecnicoId,
                                     @Valid @RequestBody CelularRequest request,
                                     @AuthenticationPrincipal Jwt jwt) {
        return service.registrar(vistoriaId, tecnicoId, request, ehAdmin(jwt));
    }

    @DeleteMapping("/{tecnicoId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ESCRITA)
    public void remover(@PathVariable Long vistoriaId, @PathVariable Long tecnicoId) {
        service.remover(vistoriaId, tecnicoId);
    }

    private boolean ehAdmin(Jwt jwt) {
        return "ADMIN".equals(jwt.getClaimAsString("perfil"));
    }
}
