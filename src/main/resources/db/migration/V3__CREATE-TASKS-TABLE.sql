CREATE TABLE tab_tasks (
    id BIGSERIAL PRIMARY KEY,
    dharma_id BIGINT NOT NULL,
    title VARCHAR(60) NOT NULL,
    description VARCHAR(200),
    karma_type VARCHAR(20) NOT NULL,
    effort_level VARCHAR(10) NOT NULL,
    status VARCHAR(10) NOT NULL DEFAULT 'NEXT',
    completed_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_dharma
        FOREIGN KEY(dharma_id)
            REFERENCES tab_dharma(id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_tasks_dharma_id ON tab_tasks(dharma_id);
CREATE INDEX idx_tasks_status ON tab_tasks(status);
CREATE INDEX idx_tasks_dharma_status ON tab_tasks(dharma_id, status);
