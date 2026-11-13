package br.com.frota.veiculo;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VeiculoRepository extends JpaRepository<Veiculo, Long> {

    boolean existsByPlaca(String placa);

    boolean existsByPlacaAndIdNot(String placa, Long id);

    @Query("select distinct v from Veiculo v left join fetch v.responsaveis order by v.placa")
    List<Veiculo> findTodosComResponsaveis();

    /** Carros em que o usuário é um dos responsáveis (já trazendo a dupla completa). */
    @Query("""
            select distinct v from Veiculo v left join fetch v.responsaveis
            where v.id in (select v2.id from Veiculo v2 join v2.responsaveis r where r.id = :usuarioId)
            order by v.placa
            """)
    List<Veiculo> findDoResponsavel(@Param("usuarioId") Long usuarioId);
}
