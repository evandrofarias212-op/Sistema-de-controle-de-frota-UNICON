package br.com.frota.manutencao;

import br.com.frota.veiculo.TipoVeiculo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface RegraManutencaoRepository extends JpaRepository<RegraManutencao, Long> {

    List<RegraManutencao> findByAtivoTrue();

    List<RegraManutencao> findAllByOrderByTipoVeiculoAscTipoManutencaoAsc();

    Optional<RegraManutencao> findByTipoVeiculoAndTipoManutencao(TipoVeiculo tipoVeiculo,
                                                                 TipoManutencao tipoManutencao);
}
