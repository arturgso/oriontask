-- Adiciona campo hidden para Dharmas e Tasks
-- Tasks herdam visibilidade do Dharma pai

ALTER TABLE tab_dharma 
ADD COLUMN hidden BOOLEAN DEFAULT FALSE NOT NULL;

ALTER TABLE tab_tasks 
ADD COLUMN hidden BOOLEAN DEFAULT FALSE NOT NULL;

-- Índice para melhorar performance de queries de Dharmas visíveis
CREATE INDEX idx_dharma_user_hidden ON tab_dharma(user_id, hidden);

-- Índice para melhorar performance de queries de Tasks visíveis
CREATE INDEX idx_tasks_dharma_hidden ON tab_tasks(dharma_id, hidden);
