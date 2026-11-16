-- Parâmetros padrão da manutenção preventiva, por tipo de veículo (editáveis pela API)
CREATE TABLE regras_manutencao (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    tipo_veiculo     VARCHAR(20) NOT NULL,
    tipo_manutencao  VARCHAR(20) NOT NULL,
    intervalo_km     INT         NULL,
    intervalo_meses  INT         NULL,
    alerta_km        INT         NOT NULL DEFAULT 1000,
    alerta_dias      INT         NOT NULL DEFAULT 15,
    ativo            BOOLEAN     NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_regra_tipo UNIQUE (tipo_veiculo, tipo_manutencao)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- A correia fica DESATIVADA até alguém definir o intervalo certo (os números abaixo são só um ponto de partida)
INSERT INTO regras_manutencao (tipo_veiculo, tipo_manutencao, intervalo_km, intervalo_meses, alerta_km, alerta_dias, ativo) VALUES
    ('CARRO', 'TROCA_OLEO',    10000,  6, 1000, 15, TRUE),
    ('CARRO', 'TROCA_CORREIA', 60000, 48, 5000, 60, FALSE),
    ('MOTO',  'TROCA_OLEO',     3000,  6,  500, 15, TRUE);

-- Intervalo personalizado de um veículo específico (sobrepõe a regra padrão)
CREATE TABLE planos_manutencao (
    id               BIGINT      NOT NULL AUTO_INCREMENT,
    veiculo_id       BIGINT      NOT NULL,
    tipo_manutencao  VARCHAR(20) NOT NULL,
    intervalo_km     INT         NULL,
    intervalo_meses  INT         NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_plano_veiculo_tipo UNIQUE (veiculo_id, tipo_manutencao),
    CONSTRAINT fk_plano_manut_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Traz os intervalos de óleo personalizados que já existiam
INSERT INTO planos_manutencao (veiculo_id, tipo_manutencao, intervalo_km, intervalo_meses)
SELECT veiculo_id, 'TROCA_OLEO', intervalo_km, intervalo_meses FROM planos_oleo;
