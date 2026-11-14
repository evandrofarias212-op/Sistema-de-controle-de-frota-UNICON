package br.com.frota.vistoria;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public final class CelularDtos {

    private CelularDtos() {
    }

    public record CelularRequest(
            @NotNull SituacaoAcessorio capa,
            @NotNull SituacaoAcessorio pelicula,
            @NotNull Boolean senhaConferida,
            @NotNull Boolean senhaTrocada,
            @Size(max = 80) String aparelho,
            @Size(max = 500) String observacao,
            @Size(max = 10) List<Long> fotoArquivoIds,
            /** Só ADMIN pode informar. Tem que ser um arquivo enviado com sensivel=true. */
            Long videoSenhaArquivoId) {
    }

    /** videoSenhaArquivoId só vem preenchido para ADMIN; os demais veem apenas temVideoSenha. */
    public record CelularResponse(
            Long id,
            Long vistoriaId,
            Long tecnicoId,
            String tecnicoNome,
            String aparelho,
            SituacaoAcessorio capa,
            SituacaoAcessorio pelicula,
            boolean senhaConferida,
            boolean senhaTrocada,
            String observacao,
            List<Long> fotoArquivoIds,
            boolean temVideoSenha,
            Long videoSenhaArquivoId) {
    }
}
