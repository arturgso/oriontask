create table tab_dharma (
    id bigint primary key,  
    user_id UUID not null,
    name varchar(20) not null unique,
    description text,
    color varchar(7) not null,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_user
        FOREIGN KEY(user_id) 
            REFERENCES tab_users(id)
            ON DELETE CASCADE
);