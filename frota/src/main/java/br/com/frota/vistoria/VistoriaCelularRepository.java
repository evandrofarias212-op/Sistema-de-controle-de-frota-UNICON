package br.com.frota.vistoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VistoriaCelularRepository extends JpaRepository<VistoriaCelular, Long> {

    List<VistoriaCelular> findByVistoriaIdOrderByIdAsc(Long vistoriaId);

    Optional<VistoriaCelular> findByVistoriaIdAndTecnicoId(Long vistoriaId, Long tecnicoId);
}
