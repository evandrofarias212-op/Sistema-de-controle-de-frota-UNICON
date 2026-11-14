-- Vistoria do celular de cada técnico (capa, película, senha conferida/trocada, fotos e vídeo protegido)
CREATE TABLE vistoria_celulares (
    id                      BIGINT       NOT NULL AUTO_INCREMENT,
    vistoria_id             BIGINT       NOT NULL,
    tecnico_id              BIGINT       NOT NULL,
    aparelho                VARCHAR(80)  NULL,
    capa                    VARCHAR(20)  NOT NULL,
    pelicula                VARCHAR(20)  NOT NULL,
    senha_conferida         BOOLEAN      NOT NULL,
    senha_trocada           BOOLEAN      NOT NULL,
    observacao              VARCHAR(500) NULL,
    video_senha_arquivo_id  BIGINT       NULL,
    criado_em               DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (id),
    CONSTRAINT uk_celular_vistoria_tecnico UNIQUE (vistoria_id, tecnico_id),
    CONSTRAINT fk_celular_vistoria FOREIGN KEY (vistoria_id) REFERENCES vistorias (id) ON DELETE CASCADE,
    CONSTRAINT fk_celular_tecnico  FOREIGN KEY (tecnico_id)  REFERENCES usuarios (id),
    CONSTRAINT fk_celular_video    FOREIGN KEY (video_senha_arquivo_id) REFERENCES arquivos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE vistoria_celular_fotos (
    celular_id  BIGINT NOT NULL,
    arquivo_id  BIGINT NOT NULL,
    PRIMARY KEY (celular_id, arquivo_id),
    CONSTRAINT fk_celular_fotos_celular FOREIGN KEY (celular_id) REFERENCES vistoria_celulares (id) ON DELETE CASCADE,
    CONSTRAINT fk_celular_fotos_arquivo FOREIGN KEY (arquivo_id) REFERENCES arquivos (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
