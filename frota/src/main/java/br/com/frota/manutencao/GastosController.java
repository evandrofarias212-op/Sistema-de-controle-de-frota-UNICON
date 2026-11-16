package br.com.frota.manutencao;

import br.com.frota.manutencao.ManutencaoDtos.RelatorioGastos;
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
public class GastosController {

    private final GastosService service;

    public GastosController(GastosService service) {
        this.service = service;
    }

    /** Combustível + manutenção por veículo e por equipe. Filtros opcionais: de, ate e equipe. */
    @GetMapping("/gastos")
    @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR','GERENTE')")
    public RelatorioGastos gastos(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate,
            @RequestParam(required = false) Equipe equipe) {
        return service.relatorio(de, ate, equipe);
    }
}
