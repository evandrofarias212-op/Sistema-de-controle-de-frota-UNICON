-- Observações do dia a dia sobre o carro (texto livre, não editável)
CREATE TABLE observacoes_veiculo (
    id          BIGINT        NOT NULL AUTO_INCREMENT,
    veiculo_id  BIGINT        NOT NULL,
    autor_id    BIGINT        NOT NULL,
    dia         DATE          NOT NULL,
    texto       VARCHAR(1000) NOT NULL,
    criado_em   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_obs_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id),
    CONSTRAINT fk_obs_autor   FOREIGN KEY (autor_id)   REFERENCES usuarios (id),
    INDEX idx_obs_veiculo_dia (veiculo_id, dia)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Leitura semanal do odômetro: uma por veículo por semana (semana começa na segunda)
CREATE TABLE km_semanais (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    veiculo_id     BIGINT       NOT NULL,
    lancado_por    BIGINT       NOT NULL,
    semana_inicio  DATE         NOT NULL,
    km             INT          NOT NULL,
    km_percorrido  INT          NULL,
    observacao     VARCHAR(300) NULL,
    criado_em      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_km_semana UNIQUE (veiculo_id, semana_inicio),
    CONSTRAINT fk_kmsem_veiculo FOREIGN KEY (veiculo_id)  REFERENCES veiculos (id),
    CONSTRAINT fk_kmsem_usuario FOREIGN KEY (lancado_por) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
