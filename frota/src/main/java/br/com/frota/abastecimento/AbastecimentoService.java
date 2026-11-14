package br.com.frota.abastecimento;

import br.com.frota.abastecimento.AbastecimentoDtos.*;
import br.com.frota.arquivo.Arquivo;
import br.com.frota.arquivo.ArquivoRepository;
import br.com.frota.usuario.Equipe;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AbastecimentoService {

    private final AbastecimentoRepository repository;
    private final VeiculoRepository veiculoRepository;
    private final ArquivoRepository arquivoRepository;
    private final UsuarioRepository usuarioRepository;

    public AbastecimentoService(AbastecimentoRepository repository,
                                VeiculoRepository veiculoRepository,
                                ArquivoRepository arquivoRepository,
                                UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.veiculoRepository = veiculoRepository;
        this.arquivoRepository = arquivoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    @Transactional
    public AbastecimentoResponse registrar(AbastecimentoRequest r, Long usuarioId, String perfil) {
        Veiculo veiculo = veiculoRepository.findById(r.veiculoId())
                .orElseThrow(() -> invalida("Veículo não encontrado"));
        boolean gestor = gestor(perfil);

        if (!gestor && !ehResponsavel(veiculo, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é responsável por esse veículo");
        }
        if (veiculo.getStatus() == StatusVeiculo.BAIXADO) {
            throw invalida("Veículo baixado não pode ser abastecido");
        }
        if (r.abastecidoEm().isAfter(LocalDateTime.now().plusMinutes(10))) {
            throw invalida("A data do abastecimento está no futuro");
        }
        if (r.km() < veiculo.getKmAtual()) {
            throw invalida("O km informado (" + r.km() + ") é menor que o km atual do veículo ("
                    + veiculo.getKmAtual() + ")");
        }

        validarFotos(r, usuarioId, gestor);

        String posto = vazioParaNulo(r.posto());
        String observacao = vazioParaNulo(r.observacao());
        Abastecimento salvo = repository.save(new Abastecimento(veiculo.getId(), usuarioId, r.abastecidoEm(),
                r.km(), r.combustivel(), r.litros(), r.valor(), posto, observacao,
                r.fotoValeId(), r.fotoBombaId(), r.fotoKmId()));

        veiculo.setKmAtual(r.km());
        String nome = usuarioRepository.findById(usuarioId).map(Usuario::getNome).orElse(null);
        return AbastecimentoResponse.de(salvo, veiculo.getPlaca(), nome);
    }

    /** somenteDoUsuarioId != null: técnico, só enxerga abastecimentos dos carros dele. */
    @Transactional(readOnly = true)
    public List<AbastecimentoResponse> listar(Long veiculoId, LocalDate de, LocalDate ate, int limite,
                                              Long somenteDoUsuarioId) {
        LocalDateTime inicio = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.plusDays(1).atStartOfDay();
        int tamanho = Math.max(1, Math.min(limite, 500));
        return respostas(repository.buscar(veiculoId, inicio, fim, somenteDoUsuarioId, PageRequest.of(0, tamanho)));
    }

    @Transactional(readOnly = true)
    public AbastecimentoResponse buscar(Long id, Long somenteDoUsuarioId) {
        Abastecimento a = repository.findById(id).orElseThrow(this::naoEncontrado);
        if (somenteDoUsuarioId != null) {
            Veiculo v = veiculoRepository.findById(a.getVeiculoId()).orElseThrow(this::naoEncontrado);
            if (!ehResponsavel(v, somenteDoUsuarioId)) {
                throw naoEncontrado(); // não revela que existe
            }
        }
        return respostas(List.of(a)).get(0);
    }

    @Transactional(readOnly = true)
    public RelatorioCombustivel relatorio(LocalDate de, LocalDate ate, Equipe equipe) {
        LocalDateTime inicio = de == null ? null : de.atStartOfDay();
        LocalDateTime fim = ate == null ? null : ate.plusDays(1).atStartOfDay();

        List<LinhaVeiculo> linhas = repository.resumoPorVeiculo(inicio, fim, equipe).stream()
                .map(o -> new LinhaVeiculo((Long) o[0], (String) o[1], (Equipe) o[2],
                        (Long) o[3], (BigDecimal) o[4], (BigDecimal) o[5]))
                .toList();

        Map<Equipe, List<LinhaVeiculo>> grupos = linhas.stream()
                .collect(Collectors.groupingBy(LinhaVeiculo::equipe, () -> new EnumMap<>(Equipe.class),
                        Collectors.toList()));
        List<LinhaEquipe> porEquipe = grupos.entrySet().stream()
                .map(e -> new LinhaEquipe(e.getKey(),
                        e.getValue().stream().mapToLong(LinhaVeiculo::abastecimentos).sum(),
                        somar(e.getValue().stream().map(LinhaVeiculo::litros).toList()),
                        somar(e.getValue().stream().map(LinhaVeiculo::valor).toList())))
                .toList();

        return new RelatorioCombustivel(de, ate,
                linhas.stream().mapToLong(LinhaVeiculo::abastecimentos).sum(),
                somar(linhas.stream().map(LinhaVeiculo::litros).toList()),
                somar(linhas.stream().map(LinhaVeiculo::valor).toList()),
                porEquipe, linhas);
    }

    /** Anula um lançamento errado. Não volta o km do veículo (isso é corrigido na edição do veículo). */
    @Transactional
    public AbastecimentoResponse anular(Long id, String motivo, Long usuarioId) {
        Abastecimento a = repository.findById(id).orElseThrow(this::naoEncontrado);
        if (a.isAnulado()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Esse abastecimento já foi anulado");
        }
        a.anular(usuarioId, motivo.trim());
        return respostas(List.of(a)).get(0);
    }

    // ---------- internos ----------

    private void validarFotos(AbastecimentoRequest r, Long usuarioId, boolean gestor) {
        Set<Long> ids = new HashSet<>(List.of(r.fotoValeId(), r.fotoBombaId(), r.fotoKmId()));
        if (ids.size() != 3) {
            throw invalida("As 3 fotos (vale, bomba e km) precisam ser arquivos diferentes");
        }
        List<Arquivo> arquivos = arquivoRepository.findAllById(ids);
        if (arquivos.size() != 3) {
            throw invalida("Alguma das fotos informadas não existe");
        }
        for (Arquivo a : arquivos) {
            if (a.isSensivel()) {
                throw invalida("Arquivo sensível não pode ser usado como foto de abastecimento");
            }
            if (!a.getTipoConteudo().startsWith("image/")) {
                throw invalida("As 3 fotos precisam ser imagens");
            }
            if (!gestor && !a.getEnviadoPor().equals(usuarioId)) {
                throw invalida("Use apenas fotos que você mesmo enviou");
            }
        }
        if (repository.contarComFotos(ids) > 0) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Alguma dessas fotos já foi usada em outro abastecimento");
        }
    }

    private List<AbastecimentoResponse> respostas(List<Abastecimento> lista) {
        Set<Long> veiculoIds = lista.stream().map(Abastecimento::getVeiculoId).collect(Collectors.toSet());
        Set<Long> usuarioIds = lista.stream().map(Abastecimento::getLancadoPor).collect(Collectors.toSet());
        Map<Long, String> placas = veiculoRepository.findAllById(veiculoIds).stream()
                .collect(Collectors.toMap(Veiculo::getId, Veiculo::getPlaca, (a, b) -> a));
        Map<Long, String> nomes = usuarioRepository.findAllById(usuarioIds).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));
        return lista.stream()
                .map(a -> AbastecimentoResponse.de(a, placas.get(a.getVeiculoId()), nomes.get(a.getLancadoPor())))
                .toList();
    }

    private boolean ehResponsavel(Veiculo veiculo, Long usuarioId) {
        return veiculo.getResponsaveis().stream().anyMatch(u -> u.getId().equals(usuarioId));
    }

    private boolean gestor(String perfil) {
        return "ADMIN".equals(perfil) || "SUPERVISOR".equals(perfil);
    }

    private BigDecimal somar(List<BigDecimal> valores) {
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private String vazioParaNulo(String texto) {
        return texto == null || texto.isBlank() ? null : texto.trim();
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Abastecimento não encontrado");
    }
}
