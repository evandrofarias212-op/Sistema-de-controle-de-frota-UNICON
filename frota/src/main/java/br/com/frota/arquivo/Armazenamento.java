package br.com.frota.arquivo;

import java.io.IOException;
import java.io.InputStream;

/**
 * Onde os bytes dos arquivos ficam guardados. Hoje é uma pasta do servidor;
 * para migrar para nuvem (S3 etc.) basta criar outra implementação.
 */
public interface Armazenamento {

    void salvar(String chave, InputStream conteudo) throws IOException;

    InputStream abrir(String chave) throws IOException;

    void apagar(String chave) throws IOException;
}
