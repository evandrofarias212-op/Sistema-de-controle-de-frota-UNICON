package br.com.frota.usuario;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria o primeiro ADMIN quando o sistema sobe com a tabela de usuários vazia. */
@Component
public class AdminInicializador implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminInicializador.class);

    private final UsuarioRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final String nome;
    private final String email;
    private final String senha;

    public AdminInicializador(UsuarioRepository repository,
                              PasswordEncoder passwordEncoder,
                              @Value("${app.admin.nome}") String nome,
                              @Value("${app.admin.email}") String email,
                              @Value("${app.admin.senha}") String senha) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.nome = nome;
        this.email = email;
        this.senha = senha;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }
        repository.save(new Usuario(nome, email.trim().toLowerCase(), passwordEncoder.encode(senha), Perfil.ADMIN));
        log.warn("Primeiro ADMIN criado ({}). TROQUE a senha padrão antes de usar em produção!", email);
    }
}
