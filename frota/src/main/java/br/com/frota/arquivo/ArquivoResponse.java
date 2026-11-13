package br.com.frota.arquivo;

import java.time.LocalDateTime;

public record ArquivoResponse(
        Long id,
        String nomeOriginal,
        String tipoConteudo,
        long tamanhoBytes,
        boolean sensivel,
        String sha256,
        LocalDateTime criadoEm) {

    public static ArquivoResponse de(Arquivo a) {
        return new ArquivoResponse(a.getId(), a.getNomeOriginal(), a.getTipoConteudo(),
                a.getTamanhoBytes(), a.isSensivel(), a.getSha256(), a.getCriadoEm());
    }
}
