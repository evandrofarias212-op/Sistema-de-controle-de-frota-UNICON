package br.com.frota.manutencao;

import br.com.frota.manutencao.ManutencaoDtos.PlanoRequest;
import br.com.frota.manutencao.ManutencaoDtos.PlanoResponse;
import br.com.frota.manutencao.ManutencaoDtos.RegraRequest;
import br.com.frota.manutencao.ManutencaoDtos.RegraResponse;
import br.com.frota.manutencao.ManutencaoDtos.SituacaoPreventiva;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class PreventivaController {

    private static final String ESCRITA = "hasAnyRole('ADMIN','SUPERVISOR')";
    private static final String LEITURA = "hasAnyRole('ADMIN','SUPERVISOR','GERENTE')";

    private final PreventivaService service;

    public PreventivaController(PreventivaService service) {
        this.service = service;
    }

    // ---------- situação (painel) ----------

    /** Situação de toda a frota (o técnico vê só os carros/motos dele). ?tipo=TROCA_CORREIA filtra um serviço. */
    @GetMapping("/preventivas")
    public List<SituacaoPreventiva> frota(@RequestParam(required = false) TipoManutencao tipo,
                                          @AuthenticationPrincipal Jwt jwt) {
        return service.situacaoDaFrota(tipo, somenteDoTecnico(jwt));
    }

    @GetMapping("/veiculos/{id}/preventivas")
    public List<SituacaoPreventiva> doVeiculo(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        return service.situacaoDoVeiculo(id, somenteDoTecnico(jwt));
    }

    // ---------- parâmetros padrão por tipo de veículo ----------

    @GetMapping("/regras-manutencao")
    @PreAuthorize(LEITURA)
    public List<RegraResponse> regras() {
        return service.listarRegras();
    }

    /** Cria ou atualiza o parâmetro (ex.: CARRO + TROCA_CORREIA a cada X km e/ou Y meses). */
    @PutMapping("/regras-manutencao")
    @PreAuthorize(ESCRITA)
    public RegraResponse salvarRegra(@Valid @RequestBody RegraRequest request) {
        return service.salvarRegra(request);
    }

    // ---------- intervalo de um veículo específico ----------

    @GetMapping("/veiculos/{id}/planos-manutencao")
    @PreAuthorize(LEITURA)
    public List<PlanoResponse> planos(@PathVariable Long id) {
        return service.listarPlanos(id);
    }

    @PutMapping("/veiculos/{id}/planos-manutencao/{tipo}")
    @PreAuthorize(ESCRITA)
    public PlanoResponse definirPlano(@PathVariable Long id, @PathVariable TipoManutencao tipo,
                                      @Valid @RequestBody PlanoRequest request) {
        return service.definirPlano(id, tipo, request);
    }

    @DeleteMapping("/veiculos/{id}/planos-manutencao/{tipo}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize(ESCRITA)
    public void removerPlano(@PathVariable Long id, @PathVariable TipoManutencao tipo) {
        service.removerPlano(id, tipo);
    }

    private Long somenteDoTecnico(Jwt jwt) {
        return "TECNICO".equals(jwt.getClaimAsString("perfil")) ? jwt.<Long>getClaim("uid") : null;
    }
}
