create or replace function update_tasks_hidden()
returns trigger as $$
begin
    if old.hidden <> new.hidden then
        update tab_tasks
        set hidden = new.hidden
        where dharma_id = new.id
        and hidden <> new.hidden;
    end if;

    return new;
end;
$$ language plpgsql;

create trigger trg_update_tasks_hidden
after update of hidden on tab_dharma
for each row
execute function update_tasks_hidden();