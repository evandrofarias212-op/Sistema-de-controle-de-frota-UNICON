CREATE TABLE manutencoes (
    id               BIGINT        NOT NULL AUTO_INCREMENT,
    veiculo_id       BIGINT        NOT NULL,
    lancado_por      BIGINT        NOT NULL,
    tipo             VARCHAR(20)   NOT NULL,
    categoria        VARCHAR(20)   NOT NULL,
    realizada_em     DATE          NOT NULL,
    km               INT           NOT NULL,
    oficina          VARCHAR(100)  NULL,
    descricao        VARCHAR(500)  NULL,
    custo            DECIMAL(10,2) NULL,
    comprovante_id   BIGINT        NULL,
    anulado_em       DATETIME      NULL,
    anulado_por      BIGINT        NULL,
    motivo_anulacao  VARCHAR(300)  NULL,
    criado_em        DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_manut_veiculo     FOREIGN KEY (veiculo_id)     REFERENCES veiculos (id),
    CONSTRAINT fk_manut_usuario     FOREIGN KEY (lancado_por)    REFERENCES usuarios (id),
    CONSTRAINT fk_manut_comprovante FOREIGN KEY (comprovante_id) REFERENCES arquivos (id),
    CONSTRAINT fk_manut_anulado_por FOREIGN KEY (anulado_por)    REFERENCES usuarios (id),
    INDEX idx_manut_veiculo (veiculo_id, realizada_em)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Intervalo de troca de óleo personalizado por veículo (sem registro = vale o padrão)
CREATE TABLE planos_oleo (
    veiculo_id       BIGINT NOT NULL,
    intervalo_km     INT    NOT NULL,
    intervalo_meses  INT    NOT NULL,
    PRIMARY KEY (veiculo_id),
    CONSTRAINT fk_plano_oleo_veiculo FOREIGN KEY (veiculo_id) REFERENCES veiculos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
