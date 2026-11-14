package br.com.frota.vistoria;

import br.com.frota.arquivo.Arquivo;
import br.com.frota.arquivo.ArquivoRepository;
import br.com.frota.usuario.Perfil;
import br.com.frota.usuario.Usuario;
import br.com.frota.usuario.UsuarioRepository;
import br.com.frota.veiculo.StatusVeiculo;
import br.com.frota.veiculo.Veiculo;
import br.com.frota.veiculo.VeiculoRepository;
import br.com.frota.vistoria.VistoriaDtos.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VistoriaService {

    private final VistoriaRepository repository;
    private final VistoriaRegistroRepository registroRepository;
    private final VistoriaAnexoRepository anexoRepository;
    private final ItemVistoriaRepository itemRepository;
    private final VeiculoRepository veiculoRepository;
    private final ArquivoRepository arquivoRepository;
    private final UsuarioRepository usuarioRepository;

    public VistoriaService(VistoriaRepository repository,
                           VistoriaRegistroRepository registroRepository,
                           VistoriaAnexoRepository anexoRepository,
                           ItemVistoriaRepository itemRepository,
                           VeiculoRepository veiculoRepository,
                           ArquivoRepository arquivoRepository,
                           UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.registroRepository = registroRepository;
        this.anexoRepository = anexoRepository;
        this.itemRepository = itemRepository;
        this.veiculoRepository = veiculoRepository;
        this.arquivoRepository = arquivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ---------- consultas ----------

    /** somenteDoUsuarioId != null: técnico, só enxerga vistorias dos carros dele. */
    @Transactional(readOnly = true)
    public List<VistoriaResumo> listar(Long veiculoId, StatusVistoria status, LocalDate de, LocalDate ate,
                                       Long somenteDoUsuarioId) {
        return repository.buscar(veiculoId, status, de, ate, somenteDoUsuarioId).stream()
                .map(v -> new VistoriaResumo(v.getId(), v.getVeiculo().getId(), v.getVeiculo().getPlaca(),
                        v.getVeiculo().getModelo(), v.getTipo(), v.getStatus(),
                        v.getDataAgendada(), v.getRealizadaEm()))
                .toList();
    }

    @Transactional(readOnly = true)
    public VistoriaDetalhe detalhar(Long id) {
        return detalhe(buscar(id));
    }

    // ---------- agenda ----------

    @Transactional
    public VistoriaDetalhe agendar(AgendarRequest r, Long usuarioId) {
        Veiculo veiculo = veiculoRepository.findById(r.veiculoId())
                .orElseThrow(() -> invalida("Veículo não encontrado"));
        if (veiculo.getStatus() == StatusVeiculo.BAIXADO) {
            throw invalida("Veículo baixado não pode ter vistoria");
        }
        Vistoria vistoria = repository.save(new Vistoria(veiculo, r.tipo(), r.dataAgendada(), usuarioId));
        return detalhe(vistoria);
    }

    @Transactional
    public VistoriaDetalhe reagendar(Long id, LocalDate data) {
        Vistoria vistoria = buscarAberta(id);
        vistoria.setDataAgendada(data);
        return detalhe(vistoria);
    }

    @Transactional
    public VistoriaDetalhe cancelar(Long id) {
        Vistoria vistoria = buscarAberta(id);
        vistoria.cancelar();
        return detalhe(vistoria);
    }

    // ---------- preenchimento ----------

    @Transactional
    public VistoriaDetalhe registrarItem(Long id, Long itemId, RegistroRequest r) {
        Vistoria vistoria = buscarAberta(id);
        ItemVistoria item = itemRepository.findById(itemId)
                .filter(ItemVistoria::isAtivo)
                .orElseThrow(() -> invalida("Item de vistoria não encontrado ou inativo"));

        String observacao = r.observacao() == null ? null : r.observacao().trim();
        if (r.estado() == EstadoItem.AVARIA && (observacao == null || observacao.isEmpty())) {
            throw invalida("Descreva a avaria na observação (" + item.getNome() + ")");
        }

        Set<Long> arquivoIds = validarArquivos(r.arquivoIds());
        VistoriaRegistro registro = registroRepository.findByVistoriaIdAndItemId(id, itemId)
                .orElseGet(() -> new VistoriaRegistro(id, itemId));
        registro.atualizar(r.estado(), observacao, arquivoIds);
        registroRepository.save(registro);
        return detalhe(vistoria);
    }

    @Transactional
    public VistoriaDetalhe adicionarAnexo(Long id, AnexoRequest r) {
        Vistoria vistoria = buscarAberta(id);
        validarArquivos(List.of(r.arquivoId()));
        if (r.tecnicoId() != null) {
            Usuario tecnico = usuarioRepository.findById(r.tecnicoId())
                    .orElseThrow(() -> invalida("Técnico não encontrado"));
            if (tecnico.getPerfil() != Perfil.TECNICO) {
                throw invalida("O usuário informado não é técnico");
            }
        }
        anexoRepository.save(new VistoriaAnexo(id, r.tipo(), r.arquivoId(), r.tecnicoId()));
        return detalhe(vistoria);
    }

    @Transactional
    public VistoriaDetalhe removerAnexo(Long id, Long anexoId) {
        Vistoria vistoria = buscarAberta(id);
        VistoriaAnexo anexo = anexoRepository.findById(anexoId)
                .filter(a -> a.getVistoriaId().equals(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Anexo não encontrado"));
        anexoRepository.delete(anexo);
        return detalhe(vistoria);
    }

    @Transactional
    public VistoriaDetalhe concluir(Long id, ConcluirRequest r, Long usuarioId) {
        Vistoria vistoria = buscarAberta(id);

        List<String> pendencias = calcularPendencias(vistoria);
        if (!pendencias.isEmpty()) {
            throw new PendenciasException(pendencias);
        }

        Veiculo veiculo = vistoria.getVeiculo();
        if (r.km() < veiculo.getKmAtual()) {
            throw invalida("O km informado (" + r.km() + ") é menor que o km atual do veículo ("
                    + veiculo.getKmAtual() + ")");
        }
        veiculo.setKmAtual(r.km());

        String observacoes = r.observacoes() == null ? null : r.observacoes().trim();
        vistoria.concluir(r.km(), observacoes, usuarioId);
        return detalhe(vistoria);
    }

    // ---------- internos ----------

    private Set<Long> validarArquivos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> unicos = new HashSet<>(ids);
        List<Arquivo> arquivos = arquivoRepository.findAllById(unicos);
        if (arquivos.size() != unicos.size()) {
            throw invalida("Algum arquivo informado não existe");
        }
        if (arquivos.stream().anyMatch(Arquivo::isSensivel)) {
            throw invalida("Arquivo sensível não pode ser usado como foto ou anexo da vistoria");
        }
        return unicos;
    }

    private List<String> calcularPendencias(Vistoria vistoria) {
        if (vistoria.getStatus() != StatusVistoria.AGENDADA) {
            return List.of();
        }
        List<String> pendencias = new ArrayList<>();

        Map<Long, VistoriaRegistro> registros = registroRepository.findByVistoriaId(vistoria.getId()).stream()
                .collect(Collectors.toMap(VistoriaRegistro::getItemId, Function.identity()));

        for (ItemVistoria item : itemRepository.findAllByOrderByOrdemAscNomeAsc()) {
            if (!item.isAtivo() || !item.isObrigatorio()) {
                continue;
            }
            VistoriaRegistro registro = registros.get(item.getId());
            if (registro == null) {
                pendencias.add("Falta registrar: " + item.getNome());
            } else if (registro.getEstado() != EstadoItem.AUSENTE && registro.getArquivoIds().isEmpty()) {
                pendencias.add("Falta foto: " + item.getNome());
            }
        }

        List<VistoriaAnexo> anexos = anexoRepository.findByVistoriaIdOrderByIdAsc(vistoria.getId());
        if (anexos.stream().noneMatch(a -> a.getTipo() == TipoAnexo.DOCUMENTO_VEICULO)) {
            pendencias.add("Falta a foto do documento do veículo");
        }
        if (vistoria.getTipo() == TipoVistoria.RETIRADA
                && anexos.stream().noneMatch(a -> a.getTipo() == TipoAnexo.TERMO_RESPONSABILIDADE)) {
            pendencias.add("Falta o termo de responsabilidade assinado");
        }
        return pendencias;
    }

    private VistoriaDetalhe detalhe(Vistoria vistoria) {
        Map<Long, VistoriaRegistro> registros = registroRepository.findByVistoriaId(vistoria.getId()).stream()
                .collect(Collectors.toMap(VistoriaRegistro::getItemId, Function.identity()));
        boolean aberta = vistoria.getStatus() == StatusVistoria.AGENDADA;

        List<ItemDaVistoria> itens = itemRepository.findAllByOrderByOrdemAscNomeAsc().stream()
                .filter(i -> registros.containsKey(i.getId()) || (aberta && i.isAtivo()))
                .map(i -> {
                    VistoriaRegistro r = registros.get(i.getId());
                    return new ItemDaVistoria(i.getId(), i.getNome(), i.getCategoria(), i.isObrigatorio(),
                            r == null ? null : r.getEstado(),
                            r == null ? null : r.getObservacao(),
                            r == null ? List.<Long>of() : r.getArquivoIds().stream().sorted().toList());
                })
                .toList();

        List<AnexoResponse> anexos = anexoRepository.findByVistoriaIdOrderByIdAsc(vistoria.getId()).stream()
                .map(a -> new AnexoResponse(a.getId(), a.getTipo(), a.getArquivoId(), a.getTecnicoId()))
                .toList();

        Veiculo v = vistoria.getVeiculo();
        return new VistoriaDetalhe(vistoria.getId(), new VeiculoResumo(v.getId(), v.getPlaca(), v.getModelo()),
                vistoria.getTipo(), vistoria.getStatus(), vistoria.getDataAgendada(), vistoria.getRealizadaEm(),
                vistoria.getRealizadaPor(), vistoria.getKm(), vistoria.getObservacoes(),
                itens, anexos, calcularPendencias(vistoria));
    }

    private Vistoria buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vistoria não encontrada"));
    }

    private Vistoria buscarAberta(Long id) {
        Vistoria vistoria = buscar(id);
        if (vistoria.getStatus() != StatusVistoria.AGENDADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa vistoria já foi concluída ou cancelada");
        }
        return vistoria;
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
