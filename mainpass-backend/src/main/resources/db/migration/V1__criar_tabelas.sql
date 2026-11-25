CREATE TABLE usuario (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    email       VARCHAR(160) NOT NULL,
    senha_hash  VARCHAR(100) NOT NULL,
    perfil      VARCHAR(20)  NOT NULL,   -- ADMIN, OPERADOR, USUARIO
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   DATETIME(6)  NOT NULL,
    CONSTRAINT uk_usuario_email UNIQUE (email)
) ENGINE=InnoDB;

CREATE TABLE ambiente (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    nome        VARCHAR(120) NOT NULL,
    descricao   VARCHAR(255),
    ativo       BOOLEAN      NOT NULL DEFAULT TRUE,
    criado_em   DATETIME(6)  NOT NULL
) ENGINE=InnoDB;

CREATE TABLE permissao_acesso (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   BIGINT      NOT NULL,
    ambiente_id  BIGINT      NOT NULL,
    valido_de    DATETIME(6) NOT NULL,
    valido_ate   DATETIME(6),
    criado_em    DATETIME(6) NOT NULL,
    CONSTRAINT fk_perm_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_perm_ambiente FOREIGN KEY (ambiente_id) REFERENCES ambiente (id),
    CONSTRAINT uk_perm_usuario_ambiente UNIQUE (usuario_id, ambiente_id)
) ENGINE=InnoDB;

CREATE TABLE qr_code (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    usuario_id   BIGINT      NOT NULL,
    ambiente_id  BIGINT      NOT NULL,
    token_hash   CHAR(64)    NOT NULL,   -- SHA-256 em hexadecimal
    status       VARCHAR(20) NOT NULL,   -- ATIVO, UTILIZADO, EXPIRADO, REVOGADO
    expira_em    DATETIME(6) NOT NULL,
    criado_em    DATETIME(6) NOT NULL,
    utilizado_em DATETIME(6),
    CONSTRAINT fk_qr_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_qr_ambiente FOREIGN KEY (ambiente_id) REFERENCES ambiente (id),
    CONSTRAINT uk_qr_token_hash UNIQUE (token_hash)
) ENGINE=InnoDB;

CREATE TABLE registro_acesso (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    qr_code_id   BIGINT,                 -- nulo quando o codigo nao existe
    usuario_id   BIGINT,
    ambiente_id  BIGINT,
    operador_id  BIGINT      NOT NULL,
    resultado    VARCHAR(20) NOT NULL,   -- AUTORIZADO, RECUSADO, CODIGO_INVALIDO
    motivo       VARCHAR(30),            -- EXPIRADO, JA_UTILIZADO, SEM_PERMISSAO...
    data_hora    DATETIME(6) NOT NULL,
    CONSTRAINT fk_reg_qr       FOREIGN KEY (qr_code_id)  REFERENCES qr_code (id),
    CONSTRAINT fk_reg_usuario  FOREIGN KEY (usuario_id)  REFERENCES usuario (id),
    CONSTRAINT fk_reg_ambiente FOREIGN KEY (ambiente_id) REFERENCES ambiente (id),
    CONSTRAINT fk_reg_operador FOREIGN KEY (operador_id) REFERENCES usuario (id)
) ENGINE=InnoDB;

CREATE INDEX idx_qr_status_expira   ON qr_code (status, expira_em);
CREATE INDEX idx_registro_data_hora ON registro_acesso (data_hora);
CREATE INDEX idx_registro_resultado ON registro_acesso (resultado);
