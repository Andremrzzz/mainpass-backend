-- Garante as colunas que a entidade Usuario espera (Hibernate ddl-auto=validate).
-- Cada coluna so e criada se NAO existir, entao o script e seguro mesmo que V1/V2 ja a tenham.
-- NAO edite V1/V2/V4 ja aplicadas (o Flyway acusa checksum diferente): corrija sempre com uma nova versao.

-- telefone VARCHAR(20)
SET @existe := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'telefone');
SET @ddl := IF(@existe = 0, 'ALTER TABLE usuario ADD COLUMN telefone VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- bloco VARCHAR(20)
SET @existe := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'bloco');
SET @ddl := IF(@existe = 0, 'ALTER TABLE usuario ADD COLUMN bloco VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- unidade VARCHAR(20)
SET @existe := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'unidade');
SET @ddl := IF(@existe = 0, 'ALTER TABLE usuario ADD COLUMN unidade VARCHAR(20) NULL', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;

-- cpf VARCHAR(14) UNIQUE (varios NULL sao permitidos pelo indice unico do MySQL)
SET @existe := (SELECT COUNT(*) FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = 'usuario' AND column_name = 'cpf');
SET @ddl := IF(@existe = 0, 'ALTER TABLE usuario ADD COLUMN cpf VARCHAR(14) NULL, ADD CONSTRAINT uk_usuario_cpf UNIQUE (cpf)', 'SELECT 1');
PREPARE stmt FROM @ddl; EXECUTE stmt; DEALLOCATE PREPARE stmt;
