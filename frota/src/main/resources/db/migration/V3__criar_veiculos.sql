CREATE TABLE veiculos (
    id         BIGINT       NOT NULL AUTO_INCREMENT,
    placa      VARCHAR(8)   NOT NULL,
    marca      VARCHAR(60)  NOT NULL,
    modelo     VARCHAR(80)  NOT NULL,
    ano        INT          NOT NULL,
    km_atual   INT          NOT NULL DEFAULT 0,
    status     VARCHAR(20)  NOT NULL DEFAULT 'ATIVO',
    equipe     VARCHAR(20)  NOT NULL,
    criado_em  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_veiculos_placa UNIQUE (placa)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 1 ou 2 técnicos por veículo (a regra de no máximo 2 fica no código)
CREATE TABLE veiculo_responsaveis (
    veiculo_id BIGINT NOT NULL,
    usuario_id BIGINT NOT NULL,
    PRIMARY KEY (veiculo_id, usuario_id),
    CONSTRAINT fk_vr_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id) ON DELETE CASCADE,
    CONSTRAINT fk_vr_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
