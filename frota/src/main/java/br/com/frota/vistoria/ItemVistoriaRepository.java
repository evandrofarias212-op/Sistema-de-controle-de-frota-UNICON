package br.com.frota.vistoria;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItemVistoriaRepository extends JpaRepository<ItemVistoria, Long> {

    List<ItemVistoria> findAllByOrderByOrdemAscNomeAsc();

    boolean existsByNome(String nome);

    boolean existsByNomeAndIdNot(String nome, Long id);
}
