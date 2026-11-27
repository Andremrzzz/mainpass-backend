-- Aprovacao do convite pela portaria.
-- Novos status: PENDENTE_APROVACAO (18 caracteres: a coluna era VARCHAR(15)) e RECUSADO.
-- Convites ja existentes continuam como estao (ATIVO etc.); apenas convites novos nascem pendentes.

ALTER TABLE convites
    MODIFY COLUMN status VARCHAR(20) NOT NULL DEFAULT 'PENDENTE_APROVACAO';

ALTER TABLE convites
    ADD COLUMN decidido_por_id BIGINT       NULL,
    ADD COLUMN decidido_em     DATETIME(6)  NULL,
    ADD COLUMN motivo_recusa   VARCHAR(255) NULL;

ALTER TABLE convites
    ADD CONSTRAINT fk_convites_decidido_por FOREIGN KEY (decidido_por_id) REFERENCES usuario (id);

-- Fila da portaria (pendentes por data de criacao)
CREATE INDEX idx_convites_pendentes ON convites (status, criado_em);
