package br.com.frota.manutencao;

import br.com.frota.arquivo.Arquivo;
import br.com.frota.arquivo.ArquivoRepository;
import br.com.frota.manutencao.ManutencaoDtos.ManutencaoRequest;
import br.com.frota.manutencao.ManutencaoDtos.ManutencaoResponse;
import br.com.frota.usuario.Usuario;
import br.com.frota.usuario.UsuarioRepository;
import br.com.frota.veiculo.StatusVeiculo;
import br.com.frota.veiculo.Veiculo;
import br.com.frota.veiculo.VeiculoRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ManutencaoService {

    /** Quanto o km informado por um técnico pode passar do km atual do carro. */
    private static final int TOLERANCIA_KM_TECNICO = 1000;

    private final ManutencaoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final ArquivoRepository arquivoRepository;
    private final UsuarioRepository usuarioRepository;

    public ManutencaoService(ManutencaoRepository repository,
                             VeiculoRepository veiculoRepository,
                             ArquivoRepository arquivoRepository,
                             UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.veiculoRepository = veiculoRepository;
        this.arquivoRepository = arquivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public ManutencaoResponse registrar(ManutencaoRequest r, Long usuarioId, String perfil) {
        Veiculo veiculo = veiculoRepository.findById(r.veiculoId())
                .orElseThrow(() -> invalida("Veículo não encontrado"));
        boolean gestor = "ADMIN".equals(perfil) || "SUPERVISOR".equals(perfil);

        if (!gestor) {
            if (!ehResponsavel(veiculo, usuarioId)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é responsável por esse veículo");
            }
            if (r.tipo() != TipoManutencao.TROCA_OLEO) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "O técnico registra apenas a troca de óleo; os demais tipos ficam com o supervisor");
            }
            if (r.comprovanteId() == null) {
                throw invalida("Envie a foto do comprovante da troca de óleo");
            }
            if (r.km() > veiculo.getKmAtual() + TOLERANCIA_KM_TECNICO) {
                throw invalida("O km informado (" + r.km() + ") está muito acima do km atual do veículo ("
                        + veiculo.getKmAtual() + ")");
            }
        }
        if (veiculo.getStatus() == StatusVeiculo.BAIXADO) {
            throw invalida("Veículo baixado não aceita lançamentos");
        }

        LocalDate data = r.realizadaEm() == null ? LocalDate.now() : r.realizadaEm();
        if (data.isAfter(LocalDate.now())) {
            throw invalida("A data não pode estar no futuro");
        }

        if (r.comprovanteId() != null) {
            validarComprovante(r.comprovanteId(), usuarioId, gestor);
        }

        if (r.tipo() != TipoManutencao.OUTRO) {
            Optional<Manutencao> anterior = repository
                    .findFirstByVeiculoIdAndTipoAndAnuladoEmIsNullOrderByRealizadaEmDescIdDesc(
                            veiculo.getId(), r.tipo());
            if (anterior.isPresent() && r.km() < anterior.get().getKm()) {
                throw invalida("O km (" + r.km() + ") é menor que o da última manutenção desse tipo ("
                        + anterior.get().getKm() + ")");
            }
        }

        CategoriaManutencao categoria = r.categoria() != null ? r.categoria()
                : (r.tipo() == TipoManutencao.TROCA_OLEO || r.tipo() == TipoManutencao.TROCA_CORREIA
                        || r.tipo() == TipoManutencao.REVISAO
                        ? CategoriaManutencao.PREVENTIVA : CategoriaManutencao.CORRETIVA);

        Manutencao salva = repository.save(new Manutencao(veiculo.getId(), usuarioId, r.tipo(), categoria, data,
                r.km(), vazioParaNulo(r.oficina()), vazioParaNulo(r.descricao()), r.custo(), r.comprovanteId()));

        if (r.km() > veiculo.getKmAtual()) {
            veiculo.setKmAtual(r.km());
        }
        String nome = usuarioRepository.findById(usuarioId).map(Usuario::getNome).orElse(null);
        return ManutencaoResponse.de(salva, veiculo.getPlaca(), nome);
    }

    /** somenteDoUsuarioId != null: técnico, só enxerga manutenções dos carros dele. */
    @Transactional(readOnly = true)
    public List<ManutencaoResponse> listar(Long veiculoId, TipoManutencao tipo, LocalDate de, LocalDate ate,
                                           int limite, Long somenteDoUsuarioId) {
        int tamanho = Math.max(1, Math.min(limite, 500));
        return respostas(repository.buscar(veiculoId, tipo, de, ate, somenteDoUsuarioId, PageRequest.of(0, tamanho)));
    }

    @Transactional(readOnly = true)
    public ManutencaoResponse buscar(Long id, Long somenteDoUsuarioId) {
        Manutencao m = repository.findById(id).orElseThrow(this::naoEncontrada);
        if (somenteDoUsuarioId != null) {
            Veiculo v = veiculoRepository.findById(m.getVeiculoId()).orElseThrow(this::naoEncontrada);
            if (!ehResponsavel(v, somenteDoUsuarioId)) {
                throw naoEncontrada();
            }
        }
        return respostas(List.of(m)).get(0);
    }

    /** Anula um lançamento errado. Não volta o km do veículo. */
    @Transactional
    public ManutencaoResponse anular(Long id, String motivo, Long usuarioId) {
        Manutencao m = repository.findById(id).orElseThrow(this::naoEncontrada);
        if (m.isAnulada()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa manutenção já foi anulada");
        }
        m.anular(usuarioId, motivo.trim());
        return respostas(List.of(m)).get(0);
    }

    // ---------- internos ----------

    private void validarComprovante(Long arquivoId, Long usuarioId, boolean gestor) {
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> invalida("Comprovante não encontrado"));
        if (arquivo.isSensivel()) {
            throw invalida("Arquivo sensível não pode ser usado como comprovante");
        }
        String tipo = arquivo.getTipoConteudo();
        if (!tipo.startsWith("image/") && !tipo.equals("application/pdf")) {
            throw invalida("O comprovante precisa ser uma imagem ou um PDF");
        }
        if (!gestor && !arquivo.getEnviadoPor().equals(usuarioId)) {
            throw invalida("Use apenas arquivos que você mesmo enviou");
        }
        if (repository.existsByComprovanteIdAndAnuladoEmIsNull(arquivoId)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Esse comprovante já foi usado em outra manutenção");
        }
    }

    private List<ManutencaoResponse> respostas(List<Manutencao> lista) {
        Set<Long> veiculoIds = lista.stream().map(Manutencao::getVeiculoId).collect(Collectors.toSet());
        Set<Long> usuarioIds = lista.stream().map(Manutencao::getLancadoPor).collect(Collectors.toSet());
        Map<Long, String> placas = veiculoRepository.findAllById(veiculoIds).stream()
                .collect(Collectors.toMap(Veiculo::getId, Veiculo::getPlaca, (a, b) -> a));
        Map<Long, String> nomes = usuarioRepository.findAllById(usuarioIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));
        return lista.stream()
                .map(m -> ManutencaoResponse.de(m, placas.get(m.getVeiculoId()), nomes.get(m.getLancadoPor())))
                .toList();
    }

    private boolean ehResponsavel(Veiculo veiculo, Long usuarioId) {
        return veiculo.getResponsaveis().stream().anyMatch(u -> u.getId().equals(usuarioId));
    }

    private String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseStatusException naoEncontrada() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Manutenção não encontrada");
    }
}
