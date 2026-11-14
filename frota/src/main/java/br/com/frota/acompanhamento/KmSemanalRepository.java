package br.com.frota.acompanhamento;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface KmSemanalRepository extends JpaRepository<KmSemanal, Long> {

    Optional<KmSemanal> findByVeiculoIdAndSemanaInicio(Long veiculoId, LocalDate semanaInicio);

    /** Leitura anterior mais recente (para calcular o km percorrido). */
    Optional<KmSemanal> findFirstByVeiculoIdAndSemanaInicioBeforeOrderBySemanaInicioDesc(Long veiculoId,
                                                                                         LocalDate semanaInicio);

    @Query("""
            select k from KmSemanal k
            where (:veiculoId is null or k.veiculoId = :veiculoId)
              and (:de is null or k.semanaInicio >= :de)
              and (:ate is null or k.semanaInicio <= :ate)
              and (:usuarioId is null or k.veiculoId in
                    (select v.id from Veiculo v join v.responsaveis r where r.id = :usuarioId))
            order by k.semanaInicio desc, k.id desc
            """)
    List<KmSemanal> buscar(@Param("veiculoId") Long veiculoId,
                           @Param("de") LocalDate de,
                           @Param("ate") LocalDate ate,
                           @Param("usuarioId") Long usuarioId,
                           Pageable pageable);
}
