package br.com.frota.veiculo;

import br.com.frota.usuario.Perfil;
import br.com.frota.usuario.Usuario;
import br.com.frota.usuario.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class VeiculoService {

    private static final Pattern PLACA_ANTIGA = Pattern.compile("^[A-Z]{3}[0-9]{4}$");
    private static final Pattern PLACA_MERCOSUL = Pattern.compile("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");

    private final VeiculoRepository repository;
    private final UsuarioRepository usuarioRepository;

    public VeiculoService(VeiculoRepository repository, UsuarioRepository usuarioRepository) {
        this.repository = repository;
        this.usuarioRepository = usuarioRepository;
    }

    /** somenteDoUsuarioId != null: lista só os carros em que esse usuário é responsável (técnico). */
    @Transactional(readOnly = true)
    public List<VeiculoResponse> listar(Long somenteDoUsuarioId) {
        List<Veiculo> veiculos = somenteDoUsuarioId == null
                ? repository.findTodosComResponsaveis()
                : repository.findDoResponsavel(somenteDoUsuarioId);
        return veiculos.stream().map(VeiculoResponse::de).toList();
    }

    @Transactional(readOnly = true)
    public VeiculoResponse buscar(Long id, Long somenteDoUsuarioId) {
        Veiculo veiculo = repository.findById(id).orElseThrow(this::naoEncontrado);
        if (somenteDoUsuarioId != null && veiculo.getResponsaveis().stream()
                .noneMatch(u -> u.getId().equals(somenteDoUsuarioId))) {
            throw naoEncontrado(); // técnico não descobre que o carro existe
        }
        return VeiculoResponse.de(veiculo);
    }

    @Transactional
    public VeiculoResponse criar(VeiculoRequest request) {
        String placa = normalizarPlaca(request.placa());
        if (repository.existsByPlaca(placa)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um veículo com essa placa");
        }
        Veiculo veiculo = new Veiculo(placa, request.marca().trim(), request.modelo().trim(),
                request.ano(), request.kmAtual(), request.status(), request.equipe());
        veiculo.setTipo(request.tipo() == null ? TipoVeiculo.CARRO : request.tipo());
        veiculo.trocarResponsaveis(buscarResponsaveis(request.responsaveisIds()));
        return VeiculoResponse.de(repository.save(veiculo));
    }

    @Transactional
    public VeiculoResponse atualizar(Long id, VeiculoRequest request) {
        Veiculo veiculo = repository.findById(id).orElseThrow(this::naoEncontrado);

        String placa = normalizarPlaca(request.placa());
        if (repository.existsByPlacaAndIdNot(placa, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um veículo com essa placa");
        }
        if (request.kmAtual() < veiculo.getKmAtual()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "O km não pode diminuir (atual: " + veiculo.getKmAtual() + ")");
        }

        veiculo.setPlaca(placa);
        veiculo.setMarca(request.marca().trim());
        veiculo.setModelo(request.modelo().trim());
        veiculo.setAno(request.ano());
        veiculo.setKmAtual(request.kmAtual());
        veiculo.setStatus(request.status());
        veiculo.setEquipe(request.equipe());
        if (request.tipo() != null) {
            veiculo.setTipo(request.tipo());
        }
        veiculo.trocarResponsaveis(buscarResponsaveis(request.responsaveisIds()));
        return VeiculoResponse.de(veiculo);
    }

    private Set<Usuario> buscarResponsaveis(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> unicos = new HashSet<>(ids);
        if (unicos.size() > 2) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Um veículo pode ter no máximo 2 responsáveis");
        }
        List<Usuario> usuarios = usuarioRepository.findAllById(unicos);
        if (usuarios.size() != unicos.size()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Responsável não encontrado");
        }
        for (Usuario u : usuarios) {
            if (u.getPerfil() != Perfil.TECNICO || !u.isAtivo()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "O usuário " + u.getNome() + " não é um técnico ativo");
            }
        }
        return new HashSet<>(usuarios);
    }

    private String normalizarPlaca(String placa) {
        String limpa = placa.trim().toUpperCase(Locale.ROOT).replaceAll("[\\s-]", "");
        if (!PLACA_ANTIGA.matcher(limpa).matches() && !PLACA_MERCOSUL.matcher(limpa).matches()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Placa inválida (use ABC1234 ou ABC1D23)");
        }
        return limpa;
    }

    private ResponseStatusException naoEncontrado() {
        return new ResponseStatusException(HttpStatus.NOT_FOUND, "Veículo não encontrado");
    }
}
