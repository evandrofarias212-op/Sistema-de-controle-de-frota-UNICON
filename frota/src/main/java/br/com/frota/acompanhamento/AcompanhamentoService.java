package br.com.frota.acompanhamento;

import br.com.frota.acompanhamento.AcompanhamentoDtos.*;
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

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AcompanhamentoService {

    private final ObservacaoRepository observacaoRepository;
    private final KmSemanalRepository kmRepository;
    private final VeiculoRepository veiculoRepository;
    private final UsuarioRepository usuarioRepository;

    public AcompanhamentoService(ObservacaoRepository observacaoRepository,
                                 KmSemanalRepository kmRepository,
                                 VeiculoRepository veiculoRepository,
                                 UsuarioRepository usuarioRepository) {
        this.observacaoRepository = observacaoRepository;
        this.kmRepository = kmRepository;
        this.veiculoRepository = veiculoRepository;
        this.usuarioRepository = usuarioRepository;
    }

    // ---------- observações diárias ----------

    @Transactional
    public ObservacaoResponse registrarObservacao(ObservacaoRequest r, Long usuarioId, String perfil) {
        Veiculo veiculo = veiculoParaLancamento(r.veiculoId(), usuarioId, perfil);
        LocalDate dia = r.data() == null ? LocalDate.now() : r.data();
        if (dia.isAfter(LocalDate.now())) {
            throw invalida("A data não pode estar no futuro");
        }
        ObservacaoVeiculo salva = observacaoRepository.save(
                new ObservacaoVeiculo(veiculo.getId(), usuarioId, dia, r.texto().trim()));
        return ObservacaoResponse.de(salva, veiculo.getPlaca(), nomeDe(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<ObservacaoResponse> listarObservacoes(Long veiculoId, LocalDate de, LocalDate ate, int limite,
                                                      Long somenteDoUsuarioId) {
        List<ObservacaoVeiculo> lista = observacaoRepository.buscar(veiculoId, de, ate, somenteDoUsuarioId,
                PageRequest.of(0, tamanho(limite)));
        Map<Long, String> placas = placas(lista.stream().map(ObservacaoVeiculo::getVeiculoId).collect(Collectors.toSet()));
        Map<Long, String> nomes = nomes(lista.stream().map(ObservacaoVeiculo::getAutorId).collect(Collectors.toSet()));
        return lista.stream()
                .map(o -> ObservacaoResponse.de(o, placas.get(o.getVeiculoId()), nomes.get(o.getAutorId())))
                .toList();
    }

    // ---------- km da semana ----------

    @Transactional
    public KmSemanalResponse registrarKm(KmSemanalRequest r, Long usuarioId, String perfil) {
        Veiculo veiculo = veiculoParaLancamento(r.veiculoId(), usuarioId, perfil);

        LocalDate referencia = r.data() == null ? LocalDate.now() : r.data();
        if (referencia.isAfter(LocalDate.now())) {
            throw invalida("A data não pode estar no futuro");
        }
        LocalDate semana = referencia.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        if (kmRepository.findByVeiculoIdAndSemanaInicio(veiculo.getId(), semana).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Já existe um lançamento de km para a semana que começa em " + semana + " nesse veículo");
        }
        if (r.km() < veiculo.getKmAtual()) {
            throw invalida("O km informado (" + r.km() + ") é menor que o km atual do veículo ("
                    + veiculo.getKmAtual() + ")");
        }

        Integer percorrido = kmRepository
                .findFirstByVeiculoIdAndSemanaInicioBeforeOrderBySemanaInicioDesc(veiculo.getId(), semana)
                .map(anterior -> r.km() - anterior.getKm())
                .orElse(null);

        String observacao = r.observacao() == null || r.observacao().isBlank() ? null : r.observacao().trim();
        KmSemanal salvo = kmRepository.save(
                new KmSemanal(veiculo.getId(), usuarioId, semana, r.km(), percorrido, observacao));

        veiculo.setKmAtual(r.km());
        return KmSemanalResponse.de(salvo, veiculo.getPlaca(), nomeDe(usuarioId));
    }

    @Transactional(readOnly = true)
    public List<KmSemanalResponse> listarKm(Long veiculoId, LocalDate de, LocalDate ate, int limite,
                                            Long somenteDoUsuarioId) {
        List<KmSemanal> lista = kmRepository.buscar(veiculoId, de, ate, somenteDoUsuarioId,
                PageRequest.of(0, tamanho(limite)));
        Map<Long, String> placas = placas(lista.stream().map(KmSemanal::getVeiculoId).collect(Collectors.toSet()));
        Map<Long, String> nomes = nomes(lista.stream().map(KmSemanal::getLancadoPor).collect(Collectors.toSet()));
        return lista.stream()
                .map(k -> KmSemanalResponse.de(k, placas.get(k.getVeiculoId()), nomes.get(k.getLancadoPor())))
                .toList();
    }

    // ---------- internos ----------

    /** Técnico só lança nos carros dele; ADMIN e SUPERVISOR em qualquer carro. */
    private Veiculo veiculoParaLancamento(Long veiculoId, Long usuarioId, String perfil) {
        Veiculo veiculo = veiculoRepository.findById(veiculoId)
                .orElseThrow(() -> invalida("Veículo não encontrado"));
        boolean gestor = "ADMIN".equals(perfil) || "SUPERVISOR".equals(perfil);
        boolean responsavel = veiculo.getResponsaveis().stream().anyMatch(u -> u.getId().equals(usuarioId));
        if (!gestor && !responsavel) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Você não é responsável por esse veículo");
        }
        if (veiculo.getStatus() == StatusVeiculo.BAIXADO) {
            throw invalida("Veículo baixado não aceita lançamentos");
        }
        return veiculo;
    }

    private Map<Long, String> placas(Set<Long> ids) {
        return veiculoRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Veiculo::getId, Veiculo::getPlaca, (a, b) -> a));
    }

    private Map<Long, String> nomes(Set<Long> ids) {
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));
    }

    private String nomeDe(Long usuarioId) {
        return usuarioRepository.findById(usuarioId).map(Usuario::getNome).orElse(null);
    }

    private int tamanho(int limite) {
        return Math.max(1, Math.min(limite, 500));
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
