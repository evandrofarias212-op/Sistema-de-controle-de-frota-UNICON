package br.com.frota.vistoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VistoriaRegistroRepository extends JpaRepository<VistoriaRegistro, Long> {

    List<VistoriaRegistro> findByVistoriaId(Long vistoriaId);

    Optional<VistoriaRegistro> findByVistoriaIdAndItemId(Long vistoriaId, Long itemId);
}
