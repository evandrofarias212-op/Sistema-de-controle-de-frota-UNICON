package br.com.frota.abastecimento;

import br.com.frota.usuario.Equipe;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface AbastecimentoRepository extends JpaRepository<Abastecimento, Long> {

    /** Filtros opcionais (null = sem filtro). usuarioId = só carros em que o técnico é responsável. */
    @Query("""
            select a from Abastecimento a
            where (:veiculoId is null or a.veiculoId = :veiculoId)
              and (:de is null or a.abastecidoEm >= :de)
              and (:ate is null or a.abastecidoEm < :ate)
              and (:usuarioId is null or a.veiculoId in
                    (select v.id from Veiculo v join v.responsaveis r where r.id = :usuarioId))
            order by a.abastecidoEm desc, a.id desc
            """)
    List<Abastecimento> buscar(@Param("veiculoId") Long veiculoId,
                               @Param("de") LocalDateTime de,
                               @Param("ate") LocalDateTime ate,
                               @Param("usuarioId") Long usuarioId,
                               Pageable pageable);

    /** Quantos abastecimentos já usam alguma dessas fotos (impede reaproveitar a mesma foto). */
    @Query("""
            select count(a) from Abastecimento a
            where a.fotoValeId in :ids or a.fotoBombaId in :ids or a.fotoKmId in :ids
            """)
    long contarComFotos(@Param("ids") Collection<Long> ids);

    /** Linhas: [veiculoId, placa, equipe, quantidade, litros, valor]. */
    @Query("""
            select v.id, v.placa, v.equipe, count(a), sum(a.litros), sum(a.valor)
            from Abastecimento a, Veiculo v
            where a.veiculoId = v.id
              and (:de is null or a.abastecidoEm >= :de)
              and (:ate is null or a.abastecidoEm < :ate)
              and (:equipe is null or v.equipe = :equipe)
            group by v.id, v.placa, v.equipe
            order by sum(a.valor) desc
            """)
    List<Object[]> resumoPorVeiculo(@Param("de") LocalDateTime de,
                                    @Param("ate") LocalDateTime ate,
                                    @Param("equipe") Equipe equipe);
}
