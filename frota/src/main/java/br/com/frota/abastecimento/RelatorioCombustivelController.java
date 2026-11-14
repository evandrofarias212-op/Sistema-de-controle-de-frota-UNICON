package br.com.frota.abastecimento;

import br.com.frota.abastecimento.AbastecimentoDtos.RelatorioCombustivel;
import br.com.frota.usuario.Equipe;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/relatorios")
public class RelatorioCombustivelController {

    private final AbastecimentoService service;

    public RelatorioCombustivelController(AbastecimentoService service) {
        this.service = service;
    }

    /** Gasto com combustível por veículo e por equipe. Filtros opcionais: de, ate (datas) e equipe. */
    @GetMapping("/combustivel")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','GERENTE')")
    public RelatorioCombustivel combustivel(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) Equipe equipe) {
        return service.relatorio(de, ate, equipe);
    }
}
