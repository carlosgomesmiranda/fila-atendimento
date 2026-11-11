-- =====================================================================
-- V3 - Dados iniciais
-- Filas padrao e usuarios para demonstracao.
-- ATENCAO: troque as senhas em ambientes produtivos.
-- =====================================================================

INSERT INTO filas (nome, prefixo, descricao, ativa)
VALUES ('Geral', 'A', 'Fila de atendimento geral', TRUE);

INSERT INTO filas (nome, prefixo, descricao, ativa)
VALUES ('Prioritaria', 'P', 'Fila de atendimento prioritario', TRUE);

-- Senhas (BCrypt):
--   admin@fila.local     -> Admin@123
--   atendente@fila.local -> Atendente@123
--   usuario@fila.local   -> Usuario@123
INSERT INTO usuarios (nome, email, senha, perfil, ativo)
VALUES ('Administrador', 'admin@fila.local',
        '$2a$10$SYL8ZlCt9U.H1gLSgDd4/O8vAwxqrB8V9nIz5LkDWWovicy95HWnS',
        'ADMINISTRADOR', TRUE);

INSERT INTO usuarios (nome, email, senha, perfil, ativo)
VALUES ('Atendente Padrao', 'atendente@fila.local',
        '$2a$10$YByAi6mdbIHKHA8KVGLM1OLB/Wb1tOXntxjH0zYwaYWvB2IFjX4Cy',
        'ATENDENTE', TRUE);

INSERT INTO usuarios (nome, email, senha, perfil, ativo)
VALUES ('Usuario Padrao', 'usuario@fila.local',
        '$2a$10$pXqdEYNZ0JwUPTR0jHZDXuqQq3TjNteLwacZJoRxjCUEOsmprobBq',
        'USUARIO', TRUE);
