package br.com.frota.veiculo;

import br.com.frota.usuario.Equipe;
import br.com.frota.usuario.Usuario;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public record VeiculoResponse(
        Long id,
        String placa,
        String marca,
        String modelo,
        int ano,
        int kmAtual,
        StatusVeiculo status,
        Equipe equipe,
        TipoVeiculo tipo,
        List<Responsavel> responsaveis,
        List<String> alertas,
        LocalDateTime criadoEm) {

    public record Responsavel(Long id, String nome) {
    }

    public static VeiculoResponse de(Veiculo v) {
        List<Responsavel> responsaveis = v.getResponsaveis().stream()
                .sorted(Comparator.comparing(Usuario::getNome))
                .map(u -> new Responsavel(u.getId(), u.getNome()))
                .toList();

        List<String> alertas = new ArrayList<>();
        if (v.getTipo() == TipoVeiculo.CARRO && v.getEquipe() == Equipe.FIBRA && responsaveis.size() < 2) {
            alertas.add("Carro da equipe FIBRA com menos de 2 técnicos");
        } else if (responsaveis.isEmpty() && v.getStatus() == StatusVeiculo.ATIVO) {
            alertas.add("Veículo ativo sem técnico responsável");
        }

        return new VeiculoResponse(v.getId(), v.getPlaca(), v.getMarca(), v.getModelo(), v.getAno(),
                v.getKmAtual(), v.getStatus(), v.getEquipe(), v.getTipo(), responsaveis, alertas, v.getCriadoEm());
    }
}
