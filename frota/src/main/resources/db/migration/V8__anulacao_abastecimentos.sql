ALTER TABLE abastecimentos
    ADD COLUMN anulado_em      DATETIME     NULL,
    ADD COLUMN anulado_por     BIGINT       NULL,
    ADD COLUMN motivo_anulacao VARCHAR(300) NULL,
    ADD CONSTRAINT fk_abast_anulado_por FOREIGN KEY (anulado_por) REFERENCES usuarios (id);
