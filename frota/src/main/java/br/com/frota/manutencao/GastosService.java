package br.com.frota.manutencao;

import br.com.frota.abastecimento.AbastecimentoRepository;
import br.com.frota.manutencao.ManutencaoDtos.GastoEquipe;
import br.com.frota.manutencao.ManutencaoDtos.GastoVeiculo;
import br.com.frota.manutencao.ManutencaoDtos.RelatorioGastos;
import br.com.frota.usuario.Equipe;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/** Junta combustível e manutenção: o "controle de gastos" da frota. */
@Service
public class GastosService {

    private static final class Acumulado {
        final Long veiculoId;
        final String placa;
        final Equipe equipe;
        BigDecimal combustivel = BigDecimal.ZERO;
        BigDecimal manutencao = BigDecimal.ZERO;

        Acumulado(Long veiculoId, String placa, Equipe equipe) {
            this.veiculoId = veiculoId;
            this.placa = placa;
            this.equipe = equipe;
        }
    }

    private final AbastecimentoRepository abastecimentoRepository;
    private final ManutencaoRepository manutencaoRepository;

    public GastosService(AbastecimentoRepository abastecimentoRepository,
                         ManutencaoRepository manutencaoRepository) {
        this.abastecimentoRepository = abastecimentoRepository;
        this.manutencaoRepository = manutencaoRepository;
    }

    @Transactional(readOnly = true)
    public RelatorioGastos relatorio(LocalDate de, LocalDate ate, Equipe equipe) {
        LocalDateTime inicio = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.plusDays(1).atStartOfDay();

        Map<Long, Acumulado> mapa = new LinkedHashMap<>();
        for (Object[] linha : abastecimentoRepository.resumoPorVeiculo(inicio, fim, equipe)) {
            Acumulado a = mapa.computeIfAbsent((Long) linha[0],
                    id -> new Acumulado(id, (String) linha[1], (Equipe) linha[2]));
            a.combustivel = a.combustivel.add((BigDecimal) linha[5]);
        }
        for (Object[] linha : manutencaoRepository.resumoPorVeiculo(de, ate, equipe)) {
            Acumulado a = mapa.computeIfAbsent((Long) linha[0],
                    id -> new Acumulado(id, (String) linha[1], (Equipe) linha[2]));
            a.manutencao = a.manutencao.add((BigDecimal) linha[4]);
        }

        List<GastoVeiculo> veiculos = mapa.values().stream()
                .map(a -> new GastoVeiculo(a.veiculoId, a.placa, a.equipe, a.combustivel, a.manutencao,
                        a.combustivel.add(a.manutencao)))
                .sorted(Comparator.comparing(GastoVeiculo::total).reversed())
                .toList();

        Map<Equipe, List<GastoVeiculo>> grupos = veiculos.stream()
                .collect(Collectors.groupingBy(GastoVeiculo::equipe, () -> new EnumMap<>(Equipe.class),
                        Collectors.toList()));
        List<GastoEquipe> porEquipe = grupos.entrySet().stream()
                .map(e -> {
                    BigDecimal comb = somar(e.getValue().stream().map(GastoVeiculo::combustivel).toList());
                    BigDecimal man = somar(e.getValue().stream().map(GastoVeiculo::manutencao).toList());
                    return new GastoEquipe(e.getKey(), comb, man, comb.add(man));
                })
                .toList();

        BigDecimal totalComb = somar(veiculos.stream().map(GastoVeiculo::combustivel).toList());
        BigDecimal totalMan = somar(veiculos.stream().map(GastoVeiculo::manutencao).toList());
        return new RelatorioGastos(de, ate, totalComb, totalMan, totalComb.add(totalMan), porEquipe, veiculos);
    }

    private BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
