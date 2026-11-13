CREATE TABLE arquivos (
    id             BIGINT       NOT NULL AUTO_INCREMENT,
    nome_original  VARCHAR(255) NOT NULL,
    tipo_conteudo  VARCHAR(100) NOT NULL,
    tamanho_bytes  BIGINT       NOT NULL,
    sha256         VARCHAR(64)  NOT NULL,
    caminho        VARCHAR(255) NOT NULL,
    sensivel       BOOLEAN      NOT NULL DEFAULT FALSE,
    enviado_por    BIGINT       NOT NULL,
    criado_em      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_arquivos_usuario FOREIGN KEY (enviado_por) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Registro de quem abriu arquivos sensíveis (ex.: vídeo da senha do celular)
CREATE TABLE acessos_arquivo (
    id           BIGINT   NOT NULL AUTO_INCREMENT,
    arquivo_id   BIGINT   NOT NULL,
    usuario_id   BIGINT   NOT NULL,
    acessado_em  DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT fk_acessos_arquivo FOREIGN KEY (arquivo_id) REFERENCES arquivos (id),
    CONSTRAINT fk_acessos_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
