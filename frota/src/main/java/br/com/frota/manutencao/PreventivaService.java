package br.com.frota.manutencao;

import br.com.frota.manutencao.ManutencaoDtos.PlanoRequest;
import br.com.frota.manutencao.ManutencaoDtos.PlanoResponse;
import br.com.frota.manutencao.ManutencaoDtos.RegraRequest;
import br.com.frota.manutencao.ManutencaoDtos.RegraResponse;
import br.com.frota.manutencao.ManutencaoDtos.SituacaoPreventiva;
import br.com.frota.veiculo.StatusVeiculo;
import br.com.frota.veiculo.Veiculo;
import br.com.frota.veiculo.VeiculoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Manutenção preventiva (troca de óleo, de correia...). Cada regra diz de quanto em quanto km e/ou meses
 * o serviço precisa ser feito, e vale para um tipo de veículo. Um veículo pode ter intervalo próprio.
 * Vence o que chegar primeiro (km ou data).
 */
@Service
public class PreventivaService {

    private final VeiculoRepository veiculoRepository;
    private final RegraManutencaoRepository regraRepository;
    private final PlanoManutencaoRepository planoRepository;
    private final ManutencaoRepository manutencaoRepository;

    public PreventivaService(VeiculoRepository veiculoRepository,
                             RegraManutencaoRepository regraRepository,
                             PlanoManutencaoRepository planoRepository,
                             ManutencaoRepository manutencaoRepository) {
        this.veiculoRepository = veiculoRepository;
        this.regraRepository = regraRepository;
        this.planoRepository = planoRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    // ---------- situação ----------

    /** Painel da frota: mais graves primeiro. tipo = filtro opcional (ex.: só TROCA_CORREIA). */
    @Transactional(readOnly = true)
    public List<SituacaoPreventiva> situacaoDaFrota(TipoManutencao tipo, Long somenteDoUsuarioId) {
        List<Veiculo> veiculos = somenteDoUsuarioId == null
                ? veiculoRepository.findTodosComResponsaveis()
                : veiculoRepository.findDoResponsavel(somenteDoUsuarioId);
        List<RegraManutencao> regras = regraRepository.findByAtivoTrue();
        Map<Long, Map<TipoManutencao, PlanoManutencao>> planos = planoRepository.findAll().stream()
                .collect(Collectors.groupingBy(PlanoManutencao::getVeiculoId,
                        Collectors.toMap(PlanoManutencao::getTipoManutencao, Function.identity())));

        return veiculos.stream()
                .filter(v -> v.getStatus() != StatusVeiculo.BAIXADO)
                .flatMap(v -> situacoes(v, regras, planos.getOrDefault(v.getId(), Map.of()), tipo).stream())
                .sorted(Comparator.comparing((SituacaoPreventiva s) -> s.situacao())
                        .thenComparing(s -> s.placa())
                        .thenComparing(s -> s.tipoManutencao()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<SituacaoPreventiva> situacaoDoVeiculo(Long veiculoId, Long somenteDoUsuarioId) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId).orElseThrow(this::naoEncontrado);
        if (somenteDoUsuarioId != null
                && veiculo.getResponsaveis().stream().noneMatch(u -> u.getId().equals(somenteDoUsuarioId))) {
            throw naoEncontrado();
        }
        Map<TipoManutencao, PlanoManutencao> planos = planoRepository.findByVeiculoId(veiculoId).stream()
                .collect(Collectors.toMap(PlanoManutencao::getTipoManutencao, Function.identity()));
        return situacoes(veiculo, regraRepository.findByAtivoTrue(), planos, null).stream()
                .sorted(Comparator.comparing((SituacaoPreventiva s) -> s.situacao())
                        .thenComparing(s -> s.tipoManutencao()))
                .toList();
    }

    // ---------- regras padrão (por tipo de veículo) ----------

    @Transactional(readOnly = true)
    public List<RegraResponse> listarRegras() {
        return regraRepository.findAllByOrderByTipoVeiculoAscTipoManutencaoAsc().stream()
                .map(RegraResponse::de).toList();
    }

    @Transactional
    public RegraResponse salvarRegra(RegraRequest r) {
        if (r.ativo() && r.intervaloKm() == null && r.intervaloMeses() == null) {
            throw invalida("Informe pelo menos um intervalo (em km ou em meses) para ativar a regra");
        }
        RegraManutencao regra = regraRepository.findByTipoVeiculoAndTipoManutencao(r.tipoVeiculo(), r.tipoManutencao())
                .orElseGet(() -> new RegraManutencao(r.tipoVeiculo(), r.tipoManutencao()));
        regra.definir(r.intervaloKm(), r.intervaloMeses(),
                r.alertaKm() == null ? 1000 : r.alertaKm(),
                r.alertaDias() == null ? 15 : r.alertaDias(),
                r.ativo());
        return RegraResponse.de(regraRepository.save(regra));
    }

    // ---------- intervalo de um veículo específico ----------

    @Transactional(readOnly = true)
    public List<PlanoResponse> listarPlanos(Long veiculoId) {
        exigirVeiculo(veiculoId);
        return planoRepository.findByVeiculoId(veiculoId).stream().map(PlanoResponse::de).toList();
    }

    @Transactional
    public PlanoResponse definirPlano(Long veiculoId, TipoManutencao tipo, PlanoRequest r) {
        exigirVeiculo(veiculoId);
        if (r.intervaloKm() == null && r.intervaloMeses() == null) {
            throw invalida("Informe pelo menos um intervalo (em km ou em meses)");
        }
        PlanoManutencao plano = planoRepository.findByVeiculoIdAndTipoManutencao(veiculoId, tipo)
                .orElseGet(() -> new PlanoManutencao(veiculoId, tipo));
        plano.definir(r.intervaloKm(), r.intervaloMeses());
        return PlanoResponse.de(planoRepository.save(plano));
    }

    /** Remove o intervalo personalizado: o veículo volta a seguir a regra padrão do tipo dele. */
    @Transactional
    public void removerPlano(Long veiculoId, TipoManutencao tipo) {
        exigirVeiculo(veiculoId);
        planoRepository.findByVeiculoIdAndTipoManutencao(veiculoId, tipo).ifPresent(planoRepository::delete);
    }

    // ---------- internos ----------

    private List<SituacaoPreventiva> situacoes(Veiculo veiculo, List<RegraManutencao> regras,
                                               Map<TipoManutencao, PlanoManutencao> planos, TipoManutencao filtro) {
        List<SituacaoPreventiva> resultado = new ArrayList<>();
        for (RegraManutencao regra : regras) {
            if (regra.getTipoVeiculo() != veiculo.getTipo()) {
                continue;
            }
            if (filtro != null && regra.getTipoManutencao() != filtro) {
                continue;
            }
            resultado.add(calcular(veiculo, regra, planos.get(regra.getTipoManutencao())));
        }
        return resultado;
    }

    private SituacaoPreventiva calcular(Veiculo veiculo, RegraManutencao regra, PlanoManutencao plano) {
        boolean personalizado = plano != null;
        Integer intervaloKm = personalizado ? plano.getIntervaloKm() : regra.getIntervaloKm();
        Integer intervaloMeses = personalizado ? plano.getIntervaloMeses() : regra.getIntervaloMeses();

        Optional<Manutencao> ultima = manutencaoRepository
                .findFirstByVeiculoIdAndTipoAndAnuladoEmIsNullOrderByRealizadaEmDescIdDesc(
                        veiculo.getId(), regra.getTipoManutencao());
        if (ultima.isEmpty()) {
            return new SituacaoPreventiva(veiculo.getId(), veiculo.getPlaca(), veiculo.getTipo(), veiculo.getEquipe(),
                    regra.getTipoManutencao(), StatusPreventiva.SEM_REGISTRO, null, null, null, null, null, null,
                    intervaloKm, intervaloMeses, personalizado);
        }

        Manutencao feita = ultima.get();
        Integer proximaKm = intervaloKm == null ? null : feita.getKm() + intervaloKm;
        LocalDate proximaData = intervaloMeses == null ? null : feita.getRealizadaEm().plusMonths(intervaloMeses);
        Integer kmRestantes = proximaKm == null ? null : proximaKm - veiculo.getKmAtual();
        Long diasRestantes = proximaData == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), proximaData);

        StatusPreventiva status;
        if ((kmRestantes != null && kmRestantes <= 0) || (diasRestantes != null && diasRestantes <= 0)) {
            status = StatusPreventiva.VENCIDO;
        } else if ((kmRestantes != null && kmRestantes <= regra.getAlertaKm())
                || (diasRestantes != null && diasRestantes <= regra.getAlertaDias())) {
            status = StatusPreventiva.VENCENDO;
        } else {
            status = StatusPreventiva.EM_DIA;
        }
        return new SituacaoPreventiva(veiculo.getId(), veiculo.getPlaca(), veiculo.getTipo(), veiculo.getEquipe(),
                regra.getTipoManutencao(), status, feita.getRealizadaEm(), feita.getKm(), proximaKm, proximaData,
                kmRestantes, diasRestantes, intervaloKm, intervaloMeses, personalizado);
    }

    private void exigirVeiculo(Long veiculoId) {
        if (!veiculoRepository.existsById(veiculoId)) {
            throw naoEncontrado();
        }
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado");
    }
}
