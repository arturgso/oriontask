CREATE TABLE tab_users (
    id UUID primary key unique not null,
    name varchar(50) not null, 
    username VARCHAR(20) not null unique,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
);