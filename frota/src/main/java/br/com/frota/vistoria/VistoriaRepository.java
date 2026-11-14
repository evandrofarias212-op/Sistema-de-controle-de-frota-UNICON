package br.com.frota.vistoria;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface VistoriaRepository extends JpaRepository<Vistoria, Long> {

    /** Todos os filtros são opcionais (null = sem filtro). usuarioId = só carros em que o técnico é responsável. */
    @Query("""
            select v from Vistoria v join fetch v.veiculo
            where (:veiculoId is null or v.veiculo.id = :veiculoId)
              and (:status is null or v.status = :status)
              and (:de is null or v.dataAgendada >= :de)
              and (:ate is null or v.dataAgendada <= :ate)
              and (:usuarioId is null or v.veiculo.id in
                    (select v2.id from Veiculo v2 join v2.responsaveis r where r.id = :usuarioId))
            order by v.dataAgendada desc, v.id desc
            """)
    List<Vistoria> buscar(@Param("veiculoId") Long veiculoId,
                          @Param("status") StatusVistoria status,
                          @Param("de") LocalDate de,
                          @Param("ate") LocalDate ate,
                          @Param("usuarioId") Long usuarioId);
}
