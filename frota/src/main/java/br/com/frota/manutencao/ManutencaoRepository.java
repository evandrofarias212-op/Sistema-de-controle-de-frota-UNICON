package br.com.frota.manutencao;

import br.com.frota.usuario.Equipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ManutencaoRepository extends JpaRepository<Manutencao, Long> {

    /** Filtros opcionais (null = sem filtro). usuarioId = só carros em que o técnico é responsável. */
    @Query("""
            select m from Manutencao m
            where (:veiculoId is null or m.veiculoId = :veiculoId)
              and (:tipo is null or m.tipo = :tipo)
              and (:de is null or m.realizadaEm >= :de)
              and (:ate is null or m.realizadaEm <= :ate)
              and (:usuarioId is null or m.veiculoId in
                    (select v.id from Veiculo v join v.responsaveis r where r.id = :usuarioId))
            order by m.realizadaEm desc, m.id desc
            """)
    List<Manutencao> buscar(@Param("veiculoId") Long veiculoId,
                            @Param("tipo") TipoManutencao tipo,
                            @Param("de") LocalDate de,
                            @Param("ate") LocalDate ate,
                            @Param("usuarioId") Long usuarioId,
                            Pageable pageable);

    /** Última troca válida (não anulada) de um tipo, para o cálculo do vencimento do óleo. */
    Optional<Manutencao> findFirstByVeiculoIdAndTipoAndAnuladoEmIsNullOrderByRealizadaEmDescIdDesc(
            Long veiculoId, TipoManutencao tipo);

    boolean existsByComprovanteIdAndAnuladoEmIsNull(Long comprovanteId);

    /** Linhas: [veiculoId, placa, equipe, quantidade, custoTotal]. Só manutenções com custo e não anuladas. */
    @Query("""
            select v.id, v.placa, v.equipe, count(m), sum(m.custo)
            from Manutencao m, Veiculo v
            where m.veiculoId = v.id
              and m.anuladoEm is null
              and m.custo is not null
              and (:de is null or m.realizadaEm >= :de)
              and (:ate is null or m.realizadaEm <= :ate)
              and (:equipe is null or v.equipe = :equipe)
            group by v.id, v.placa, v.equipe
            """)
    List<Object[]> resumoPorVeiculo(@Param("de") LocalDate de,
                                    @Param("ate") LocalDate ate,
                                    @Param("equipe") Equipe equipe);
}
