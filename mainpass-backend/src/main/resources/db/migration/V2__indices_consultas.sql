CREATE INDEX idx_qr_usuario_criado ON qr_code (usuario_id, criado_em);
CREATE INDEX idx_permissao_vigencia ON permissao_acesso (usuario_id, ambiente_id, valido_de, valido_ate);
CREATE INDEX idx_registro_usuario_data ON registro_acesso (usuario_id, data_hora);
CREATE INDEX idx_registro_ambiente_data ON registro_acesso (ambiente_id, data_hora);
