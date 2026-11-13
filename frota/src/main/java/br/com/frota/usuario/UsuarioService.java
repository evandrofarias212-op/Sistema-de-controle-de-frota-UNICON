package br.com.frota.usuario;

import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public Usuario criar(UsuarioRequest request) {
        if (request.perfil() == Perfil.TECNICO && request.equipe() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Técnico precisa ter uma equipe (SUPORTE ou FIBRA)");
        }
        String email = request.email().trim().toLowerCase();
        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Já existe um usuário com esse e-mail");
        }
        Usuario usuario = new Usuario(
                request.nome().trim(),
                email,
                passwordEncoder.encode(request.senha()),
                request.perfil());
        usuario.setEquipe(request.equipe());
        return repository.save(usuario);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listar() {
        return repository.findAll();
    }

    @Transactional
    public Usuario alterarAtivo(Long id, boolean ativo) {
        Usuario usuario = buscar(id);
        usuario.setAtivo(ativo);
        return usuario;
    }

    @Transactional
    public Usuario alterarEquipe(Long id, Equipe equipe) {
        Usuario usuario = buscar(id);
        usuario.setEquipe(equipe);
        return usuario;
    }

    private Usuario buscar(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuário não encontrado"));
    }
}
