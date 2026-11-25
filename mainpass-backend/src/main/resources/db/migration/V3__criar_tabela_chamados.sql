CREATE TABLE IF NOT EXISTS chamados (
                                        id BIGINT NOT NULL AUTO_INCREMENT,
                                        solicitante_id BIGINT NOT NULL,
                                        assunto VARCHAR(150) NOT NULL,
    mensagem VARCHAR(2000) NOT NULL,
    status VARCHAR(15) NOT NULL DEFAULT 'ABERTO',
    criado_em DATETIME(6) NOT NULL,
    resolvido_em DATETIME(6) NULL,
    resolvido_por_id BIGINT NULL,
    CONSTRAINT pk_chamados PRIMARY KEY (id),
    CONSTRAINT fk_chamados_solicitante FOREIGN KEY (solicitante_id) REFERENCES usuario (id),
    CONSTRAINT fk_chamados_resolvido_por FOREIGN KEY (resolvido_por_id) REFERENCES usuario (id)
    ) ENGINE=InnoDB;

CREATE INDEX idx_chamados_status ON chamados (status);