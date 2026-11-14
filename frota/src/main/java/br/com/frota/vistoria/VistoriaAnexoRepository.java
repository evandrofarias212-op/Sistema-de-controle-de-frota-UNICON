package br.com.frota.vistoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VistoriaAnexoRepository extends JpaRepository<VistoriaAnexo, Long> {

    List<VistoriaAnexo> findByVistoriaIdOrderByIdAsc(Long vistoriaId);
}
