-- DADOS DE EXEMPLO (somente perfil dev). Migration repetivel: roda de novo se este arquivo mudar.
-- Logins:  admin@acesso.com / admin123  |  operador@acesso.com / operador123  |  usuario@acesso.com / usuario123

INSERT IGNORE INTO usuario (nome, email, senha_hash, perfil, ativo, criado_em) VALUES
('Administrador', 'admin@acesso.com',    '$2a$10$xLyfgp2M7lpyOeO1hUVTpuYrDzE6QJf2ZdCo2vaiVnaWXoiYKLlSC', 'ADMIN',    TRUE, UTC_TIMESTAMP(6)),
('Operador',      'operador@acesso.com', '$2a$10$626DNTIZdsYGuxfEcIl2HeQHhrEn.RMk/DJu9ZLjzhucE09uwTSQa', 'OPERADOR', TRUE, UTC_TIMESTAMP(6)),
('Usuario Teste', 'usuario@acesso.com',  '$2a$10$qQbOXkTmrtF84lIfSeIJkONLwCF.rlTR4ZfCWUA7ovzP1MpA/FRQq', 'USUARIO',  TRUE, UTC_TIMESTAMP(6));

INSERT INTO ambiente (nome, descricao, ativo, criado_em)
SELECT 'Laboratorio 1', 'Laboratorio de informatica', TRUE, UTC_TIMESTAMP(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ambiente WHERE nome = 'Laboratorio 1');

INSERT INTO ambiente (nome, descricao, ativo, criado_em)
SELECT 'Auditorio', 'Auditorio principal', TRUE, UTC_TIMESTAMP(6) FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM ambiente WHERE nome = 'Auditorio');

-- O usuario de teste so tem permissao no Laboratorio 1 (o Auditorio serve para testar acesso negado).
INSERT IGNORE INTO permissao_acesso (usuario_id, ambiente_id, valido_de, valido_ate, criado_em)
SELECT u.id, a.id, UTC_TIMESTAMP(6), NULL, UTC_TIMESTAMP(6)
FROM usuario u JOIN ambiente a
WHERE u.email = 'usuario@acesso.com' AND a.nome = 'Laboratorio 1';
