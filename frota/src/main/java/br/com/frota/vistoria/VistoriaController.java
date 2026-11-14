package br.com.frota.vistoria;

import br.com.frota.vistoria.VistoriaDtos.*;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/vistorias")
public class VistoriaController {

    private static final String ESCRITA = "hasAnyRole('ADMIN','SUPERVISOR')";
    private static final String LEITURA_COMPLETA = "hasAnyRole('ADMIN','SUPERVISOR','GERENTE')";

    private final VistoriaService service;

    public VistoriaController(VistoriaService service) {
        this.service = service;
    }

    /** Agenda: todos os perfis veem; o técnico só vê os carros dele (e só o resumo, sem fotos). */
    @GetMapping
    public List<VistoriaResumo> listar(
            @RequestParam(required = false) Long veiculoId,
            @RequestParam(required = false) StatusVistoria status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @AuthenticationPrincipal Jwt jwt) {
        Long somenteDoUsuario = "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
        return service.listar(veiculoId, status, de, ate, somenteDoUsuario);
    }

    @GetMapping("/{id}")
    @PreAuthorize(LEITURA_COMPLETA)
    public VistoriaDetalhe detalhar(@PathVariable Long id) {
        return service.detalhar(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe agendar(@Valid @RequestBody AgendarRequest request, @AuthenticationPrincipal Jwt jwt) {
        return service.agendar(request, jwt.<Long>getClaim("uid"));
    }

    @PatchMapping("/{id}/agenda")
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe reagendar(@PathVariable Long id, @Valid @RequestBody ReagendarRequest request) {
        return service.reagendar(id, request.dataAgendada());
    }

    @PostMapping("/{id}/cancelar")
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe cancelar(@PathVariable Long id) {
        return service.cancelar(id);
    }

    @PutMapping("/{id}/itens/{itemId}")
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe registrarItem(@PathVariable Long id, @PathVariable Long itemId,
                                         @Valid @RequestBody RegistroRequest request) {
        return service.registrarItem(id, itemId, request);
    }

    @PostMapping("/{id}/anexos")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe adicionarAnexo(@PathVariable Long id, @Valid @RequestBody AnexoRequest request) {
        return service.adicionarAnexo(id, request);
    }

    @DeleteMapping("/{id}/anexos/{anexoId}")
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe removerAnexo(@PathVariable Long id, @PathVariable Long anexoId) {
        return service.removerAnexo(id, anexoId);
    }

    @PostMapping("/{id}/concluir")
    @PreAuthorize(ESCRITA)
    public VistoriaDetalhe concluir(@PathVariable Long id, @Valid @RequestBody ConcluirRequest request,
                                    @AuthenticationPrincipal Jwt jwt) {
        return service.concluir(id, request, jwt.<Long>getClaim("uid"));
    }
}
