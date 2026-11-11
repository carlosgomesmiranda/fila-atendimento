-- =====================================================================
-- V2 - Indices para consultas de fila, historico e relatorios
-- =====================================================================

CREATE INDEX idx_senhas_fila_status_data
    ON senhas (fila_id, status, data_geracao);

CREATE INDEX idx_senhas_data_referencia
    ON senhas (data_referencia);

CREATE INDEX idx_senhas_status
    ON senhas (status);

CREATE INDEX idx_senhas_numero
    ON senhas (numero);

CREATE INDEX idx_atendimentos_inicio
    ON atendimentos (inicio);

CREATE INDEX idx_atendimentos_atendente
    ON atendimentos (atendente_id);

CREATE INDEX idx_senha_eventos_senha
    ON senha_eventos (senha_id, ocorrido_em);
