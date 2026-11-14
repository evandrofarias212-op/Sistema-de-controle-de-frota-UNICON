package br.com.frota.acompanhamento;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ObservacaoRepository extends JpaRepository<ObservacaoVeiculo, Long> {

    /** Filtros opcionais (null = sem filtro). usuarioId = só carros em que o técnico é responsável. */
    @Query("""
            select o from ObservacaoVeiculo o
            where (:veiculoId is null or o.veiculoId = :veiculoId)
              and (:de is null or o.dia >= :de)
              and (:ate is null or o.dia <= :ate)
              and (:usuarioId is null or o.veiculoId in
                    (select v.id from Veiculo v join v.responsaveis r where r.id = :usuarioId))
            order by o.dia desc, o.id desc
            """)
    List<ObservacaoVeiculo> buscar(@Param("veiculoId") Long veiculoId,
                                   @Param("de") LocalDate de,
                                   @Param("ate") LocalDate ate,
                                   @Param("usuarioId") Long usuarioId,
                                   Pageable pageable);
}
