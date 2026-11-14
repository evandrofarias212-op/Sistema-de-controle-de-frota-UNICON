package br.com.frota.vistoria;

import java.util.List;

/** Lançada quando se tenta concluir uma vistoria que ainda tem coisas faltando. */
public class PendenciasException extends RuntimeException {

    private final List<String> pendencias;

    public PendenciasException(List<String> pendencias) {
        super("A vistoria ainda tem pendências");
        this.pendencias = pendencias;
    }

    public List<String> getPendencias() {
        return pendencias;
    }
}
