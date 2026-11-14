package br.com.frota.vistoria;

import br.com.frota.arquivo.Arquivo;
import br.com.frota.arquivo.ArquivoRepository;
import br.com.frota.usuario.Perfil;
import br.com.frota.usuario.Usuario;
import br.com.frota.usuario.UsuarioRepository;
import br.com.frota.vistoria.CelularDtos.CelularRequest;
import br.com.frota.vistoria.CelularDtos.CelularResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class VistoriaCelularService {

    private final VistoriaCelularRepository repository;
    private final VistoriaRepository vistoriaRepository;
    private final UsuarioRepository usuarioRepository;
    private final ArquivoRepository arquivoRepository;

    public VistoriaCelularService(VistoriaCelularRepository repository,
                                  VistoriaRepository vistoriaRepository,
                                  UsuarioRepository usuarioRepository,
                                  ArquivoRepository arquivoRepository) {
        this.repository = repository;
        this.vistoriaRepository = vistoriaRepository;
        this.usuarioRepository = usuarioRepository;
        this.arquivoRepository = arquivoRepository;
    }

    @Transactional(readOnly = true)
    public List<CelularResponse> listar(Long vistoriaId, boolean admin) {
        if (!vistoriaRepository.existsById(vistoriaId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Vistoria não encontrada");
        }
        List<VistoriaCelular> celulares = repository.findByVistoriaIdOrderByIdAsc(vistoriaId);
        Map<Long, String> nomes = nomesDosTecnicos(celulares);
        return celulares.stream().map(c -> resposta(c, nomes.get(c.getTecnicoId()), admin)).toList();
    }

    @Transactional
    public CelularResponse registrar(Long vistoriaId, Long tecnicoId, CelularRequest r, boolean admin) {
        Vistoria vistoria = vistoriaRepository.findById(vistoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vistoria não encontrada"));
        exigirAberta(vistoria);

        Usuario tecnico = usuarioRepository.findById(tecnicoId)
                .orElseThrow(() -> invalida("Técnico não encontrado"));
        if (tecnico.getPerfil() != Perfil.TECNICO) {
            throw invalida("O usuário informado não é técnico");
        }
        if (r.videoSenhaArquivoId() != null && !admin) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Só ADMIN pode anexar o vídeo da senha");
        }

        VistoriaCelular celular = repository.findByVistoriaIdAndTecnicoId(vistoriaId, tecnicoId)
                .orElseGet(() -> new VistoriaCelular(vistoriaId, tecnicoId));

        String aparelho = r.aparelho() == null || r.aparelho().isBlank() ? null : r.aparelho().trim();
        String observacao = r.observacao() == null || r.observacao().isBlank() ? null : r.observacao().trim();
        celular.atualizar(aparelho, r.capa(), r.pelicula(), r.senhaConferida(), r.senhaTrocada(),
                observacao, validarFotos(r.fotoArquivoIds()));

        // Só o ADMIN mexe no vídeo; quando outro perfil edita, o vídeo que já existe é preservado.
        if (admin) {
            celular.setVideoSenhaArquivoId(validarVideo(r.videoSenhaArquivoId()));
        }

        VistoriaCelular salvo = repository.save(celular);
        return resposta(salvo, tecnico.getNome(), admin);
    }

    @Transactional
    public void remover(Long vistoriaId, Long tecnicoId) {
        Vistoria vistoria = vistoriaRepository.findById(vistoriaId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vistoria não encontrada"));
        exigirAberta(vistoria);
        VistoriaCelular celular = repository.findByVistoriaIdAndTecnicoId(vistoriaId, tecnicoId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Registro de celular não encontrado"));
        repository.delete(celular);
    }

    // ---------- internos ----------

    private Set<Long> validarFotos(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Set.of();
        }
        Set<Long> unicos = new HashSet<>(ids);
        List<Arquivo> arquivos = arquivoRepository.findAllById(unicos);
        if (arquivos.size() != unicos.size()) {
            throw invalida("Alguma foto informada não existe");
        }
        if (arquivos.stream().anyMatch(Arquivo::isSensivel)) {
            throw invalida("Arquivo sensível não pode ser usado como foto comum");
        }
        return unicos;
    }

    private Long validarVideo(Long arquivoId) {
        if (arquivoId == null) {
            return null;
        }
        Arquivo arquivo = arquivoRepository.findById(arquivoId)
                .orElseThrow(() -> invalida("Vídeo informado não existe"));
        if (!arquivo.isSensivel()) {
            throw invalida("O vídeo da senha precisa ser enviado como arquivo sensível (sensivel=true)");
        }
        if (!arquivo.getTipoConteudo().startsWith("video/")) {
            throw invalida("O arquivo informado não é um vídeo");
        }
        return arquivoId;
    }

    private void exigirAberta(Vistoria vistoria) {
        if (vistoria.getStatus() != StatusVistoria.AGENDADA) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Essa vistoria já foi concluída ou cancelada");
        }
    }

    private Map<Long, String> nomesDosTecnicos(List<VistoriaCelular> celulares) {
        Set<Long> ids = celulares.stream().map(VistoriaCelular::getTecnicoId).collect(Collectors.toSet());
        return usuarioRepository.findAllById(ids).stream()
                .collect(Collectors.toMap(Usuario::getId, Usuario::getNome, (a, b) -> a));
    }

    private CelularResponse resposta(VistoriaCelular c, String tecnicoNome, boolean admin) {
        return new CelularResponse(c.getId(), c.getVistoriaId(), c.getTecnicoId(), tecnicoNome,
                c.getAparelho(), c.getCapa(), c.getPelicula(), c.isSenhaConferida(), c.isSenhaTrocada(),
                c.getObservacao(), c.getFotoArquivoIds().stream().sorted().toList(),
                c.getVideoSenhaArquivoId() != null,
                admin ? c.getVideoSenhaArquivoId() : null);
    }

    private ResponseStatusException invalida(String mensagem) {
        return new ResponseStatusException(HttpStatus.BAD_REQUEST, mensagem);
    }
}
