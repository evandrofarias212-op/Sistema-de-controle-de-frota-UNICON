-- Catálogo de itens que são fotografados/conferidos na vistoria (editável pela API)
CREATE TABLE itens_vistoria (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    nome         VARCHAR(80) NOT NULL,
    categoria    VARCHAR(20) NOT NULL,
    obrigatorio  BOOLEAN     NOT NULL DEFAULT TRUE,
    ativo        BOOLEAN     NOT NULL DEFAULT TRUE,
    ordem        INT         NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT uk_itens_vistoria_nome UNIQUE (nome)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

INSERT INTO itens_vistoria (nome, categoria, obrigatorio, ordem) VALUES
    ('Frente',                  'EXTERNO',   TRUE,  10),
    ('Traseira',                'EXTERNO',   TRUE,  20),
    ('Lateral esquerda',        'EXTERNO',   TRUE,  30),
    ('Lateral direita',         'EXTERNO',   TRUE,  40),
    ('Teto',                    'EXTERNO',   TRUE,  50),
    ('Pneu dianteiro esquerdo', 'EXTERNO',   TRUE,  60),
    ('Pneu dianteiro direito',  'EXTERNO',   TRUE,  70),
    ('Pneu traseiro esquerdo',  'EXTERNO',   TRUE,  80),
    ('Pneu traseiro direito',   'EXTERNO',   TRUE,  90),
    ('Painel e km',             'INTERNO',   TRUE, 100),
    ('Bancos e interior',       'INTERNO',   TRUE, 110),
    ('Porta-malas',             'INTERNO',   TRUE, 120),
    ('Step',                    'ACESSORIO', TRUE, 200),
    ('Triangulo',               'ACESSORIO', TRUE, 210),
    ('Macaco',                  'ACESSORIO', TRUE, 220),
    ('Chave de roda',           'ACESSORIO', TRUE, 230);

CREATE TABLE vistorias (
    id             BIGINT        NOT NULL AUTO_INCREMENT,
    veiculo_id     BIGINT        NOT NULL,
    tipo           VARCHAR(20)   NOT NULL,
    status         VARCHAR(20)   NOT NULL DEFAULT 'AGENDADA',
    data_agendada  DATE          NOT NULL,
    realizada_em   DATETIME      NULL,
    realizada_por  BIGINT        NULL,
    km             INT           NULL,
    observacoes    VARCHAR(1000) NULL,
    criado_por     BIGINT        NOT NULL,
    criado_em      DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_vistorias_veiculo       FOREIGN KEY (veiculo_id)    REFERENCES veiculos (id),
    CONSTRAINT fk_vistorias_realizada_por FOREIGN KEY (realizada_por) REFERENCES usuarios (id),
    CONSTRAINT fk_vistorias_criado_por    FOREIGN KEY (criado_por)    REFERENCES usuarios (id),
    INDEX idx_vistorias_veiculo (veiculo_id),
    INDEX idx_vistorias_data (data_agendada)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Um registro por item conferido na vistoria (estado + observação + fotos)
CREATE TABLE vistoria_registros (
    id           BIGINT       NOT NULL AUTO_INCREMENT,
    vistoria_id  BIGINT       NOT NULL,
    item_id      BIGINT       NOT NULL,
    estado       VARCHAR(20)  NOT NULL,
    observacao   VARCHAR(500) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_registro_vistoria_item UNIQUE (vistoria_id, item_id),
    CONSTRAINT fk_registro_vistoria FOREIGN KEY (vistoria_id) REFERENCES vistorias (id) ON DELETE CASCADE,
    CONSTRAINT fk_registro_item     FOREIGN KEY (item_id)     REFERENCES itens_vistoria (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vistoria_registro_fotos (
    registro_id BIGINT NOT NULL,
    arquivo_id  BIGINT NOT NULL,
    PRIMARY KEY (registro_id, arquivo_id),
    CONSTRAINT fk_registro_fotos_registro FOREIGN KEY (registro_id) REFERENCES vistoria_registros (id) ON DELETE CASCADE,
    CONSTRAINT fk_registro_fotos_arquivo  FOREIGN KEY (arquivo_id)  REFERENCES arquivos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Documento do veículo, termo de responsabilidade assinado etc.
CREATE TABLE vistoria_anexos (
    id           BIGINT      NOT NULL AUTO_INCREMENT,
    vistoria_id  BIGINT      NOT NULL,
    tipo         VARCHAR(30) NOT NULL,
    arquivo_id   BIGINT      NOT NULL,
    tecnico_id   BIGINT      NULL,
    criado_em    DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_anexo_vistoria FOREIGN KEY (vistoria_id) REFERENCES vistorias (id) ON DELETE CASCADE,
    CONSTRAINT fk_anexo_arquivo  FOREIGN KEY (arquivo_id)  REFERENCES arquivos (id),
    CONSTRAINT fk_anexo_tecnico  FOREIGN KEY (tecnico_id)  REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
