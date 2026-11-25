-- Funcionalidades do Convidado: convites (vinculo morador <-> convidado) e chat por convite.
-- PREMISSA: as tabelas existentes se chamam `usuario` (confirmado pela entidade) e `ambiente`, com chave `id BIGINT`.
-- Se os nomes/tipos forem outros, ajuste as REFERENCES abaixo (o MySQL recusa a FK se o tipo divergir).

CREATE TABLE convites (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    morador_id    BIGINT       NOT NULL,
    convidado_id  BIGINT       NOT NULL,
    ambiente_id   BIGINT       NOT NULL,
    data_inicio   DATETIME(6)  NOT NULL,
    data_fim      DATETIME(6)  NOT NULL,
    status        VARCHAR(15)  NOT NULL DEFAULT 'ATIVO',
    usos          INT          NOT NULL DEFAULT 0,
    usos_max      INT          NOT NULL DEFAULT 1,
    observacao    VARCHAR(255) NULL,
    criado_em     DATETIME(6)  NOT NULL,
    cancelado_em  DATETIME(6)  NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_convites_morador   FOREIGN KEY (morador_id)   REFERENCES usuario (id),
    CONSTRAINT fk_convites_convidado FOREIGN KEY (convidado_id) REFERENCES usuario (id),
    CONSTRAINT fk_convites_ambiente  FOREIGN KEY (ambiente_id)  REFERENCES ambiente (id),
    CONSTRAINT chk_convites_janela   CHECK (data_fim > data_inicio),
    CONSTRAINT chk_convites_usos     CHECK (usos >= 0 AND usos_max >= 1 AND usos <= usos_max)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Listagem do morador (mais recentes primeiro) e contagem de convites vigentes
CREATE INDEX idx_convites_morador   ON convites (morador_id, criado_em);
CREATE INDEX idx_convites_vigencia  ON convites (morador_id, status, data_fim);
-- Area do convidado
CREATE INDEX idx_convites_convidado ON convites (convidado_id, data_inicio);
CREATE INDEX idx_convites_ambiente  ON convites (ambiente_id);

CREATE TABLE mensagens_convite (
    id            BIGINT       NOT NULL AUTO_INCREMENT,
    convite_id    BIGINT       NOT NULL,
    remetente_id  BIGINT       NOT NULL,
    tipo          VARCHAR(25)  NOT NULL DEFAULT 'TEXTO',
    conteudo      VARCHAR(500) NOT NULL,
    criado_em     DATETIME(6)  NOT NULL,
    lida          TINYINT(1)   NOT NULL DEFAULT 0,
    PRIMARY KEY (id),
    CONSTRAINT fk_mensagens_convite   FOREIGN KEY (convite_id)   REFERENCES convites (id) ON DELETE CASCADE,
    CONSTRAINT fk_mensagens_remetente FOREIGN KEY (remetente_id) REFERENCES usuario (id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Leitura incremental (WHERE convite_id = ? AND id > ?) e "marcar como lidas"
CREATE INDEX idx_mensagens_convite_lida ON mensagens_convite (convite_id, lida, remetente_id);

-- ---------------------------------------------------------------------------
-- CONFIRA ANTES DE RODAR:
--
-- 1) `usuario.perfil` e VARCHAR(20) na entidade. Se V1/V2 o criaram como ENUM ou com CHECK
--    (ex.: ENUM('ADMIN','OPERADOR','USUARIO')), inclua CONVIDADO, senao o INSERT do convidado falha:
--      ALTER TABLE usuario MODIFY perfil ENUM('ADMIN','OPERADOR','USUARIO','CONVIDADO') NOT NULL;
--    (VARCHAR sem CHECK: nada a fazer)
--
-- 2) A tabela `ambiente` precisa ter `id BIGINT` (tipo igual ao da FK) — confirme no V1/V2.
-- ---------------------------------------------------------------------------
