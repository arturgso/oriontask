create table if not exists tab_refresh_tokens (
    id uuid primary key,
    user_id uuid not null unique,
    token_hash varchar(256) not null,
    expiration_at timestamptz not null,
    created_at timestamptz not null,
    constraint fk_user
        foreign key (user_id)
            references tab_users(id)
                on delete cascade
);