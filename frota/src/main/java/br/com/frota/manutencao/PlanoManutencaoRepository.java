package br.com.frota.manutencao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PlanoManutencaoRepository extends JpaRepository<PlanoManutencao, Long> {

    List<PlanoManutencao> findByVeiculoId(Long veiculoId);

    Optional<PlanoManutencao> findByVeiculoIdAndTipoManutencao(Long veiculoId, TipoManutencao tipoManutencao);
}
