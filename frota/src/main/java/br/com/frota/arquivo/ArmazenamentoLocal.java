package br.com.frota.arquivo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Component
public class ArmazenamentoLocal implements Armazenamento {

    private final Path raiz;

    public ArmazenamentoLocal(@Value("${app.storage.dir:./frota-storage}") String dir) throws IOException {
        this.raiz = Paths.get(dir).toAbsolutePath().normalize();
        Files.createDirectories(raiz);
    }

    @Override
    public void salvar(String chave, InputStream conteudo) throws IOException {
        Path destino = resolver(chave);
        Files.createDirectories(destino.getParent());
        Files.copy(conteudo, destino, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public InputStream abrir(String chave) throws IOException {
        return Files.newInputStream(resolver(chave));
    }

    @Override
    public void apagar(String chave) throws IOException {
        Files.deleteIfExists(resolver(chave));
    }

    private Path resolver(String chave) {
        Path caminho = raiz.resolve(chave).normalize();
        if (!caminho.startsWith(raiz)) {
            throw new IllegalArgumentException("Chave de arquivo inválida");
        }
        return caminho;
    }
}
