package br.com.frota.vistoria;

import br.com.frota.vistoria.VistoriaDtos.ItemRequest;
import br.com.frota.vistoria.VistoriaDtos.ItemResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class ItemVistoriaService {

    private final ItemVistoriaRepository repository;

    public ItemVistoriaService(ItemVistoriaRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<ItemResponse> listar() {
        return repository.findAllByOrderByOrdemAscNomeAsc().stream().map(ItemResponse::de).toList();
    }

    @Transactional
    public ItemResponse criar(ItemRequest r) {
        String nome = r.nome().trim();
        if (repository.existsByNome(nome)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um item com esse nome");
        }
        ItemVistoria item = new ItemVistoria(nome, r.categoria(), r.obrigatorio(), r.ativo(), r.ordem());
        return ItemResponse.de(repository.save(item));
    }

    @Transactional
    public ItemResponse atualizar(Long id, ItemRequest r) {
        ItemVistoria item = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Item não encontrado"));
        String nome = r.nome().trim();
        if (repository.existsByNomeAndIdNot(nome, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um item com esse nome");
        }
        item.atualizar(nome, r.categoria(), r.obrigatorio(), r.ativo(), r.ordem());
        return ItemResponse.de(item);
    }
}
