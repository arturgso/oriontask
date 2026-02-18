# OrionTask Backend

API backend do OrionTask, um gerenciador de tarefas orientado a propósito (Dharma/Karma), com foco em simplicidade.

## Resumo
- Stack: Spring Boot 4 + Java 21 + PostgreSQL + Flyway.
- Base da API: `/api/v1`.
- Banco: PostgreSQL (configurado por variáveis de ambiente).
- Execução local: Gradle (`./gradlew bootRun`) ou Docker Compose.

## Funcionalidades
- Dharmas: organização de tarefas por áreas de vida.
- Tasks/Karma: acompanhamento de tarefas e consequências.
- Regra de produto: foco em poucas tarefas visíveis na Agora.

## Pré-requisitos
- Java 21
- Docker e Docker Compose (opcional, para subir a stack em containers)

## Executar localmente (Gradle)
1. Configure as variáveis de ambiente de banco (pode usar `.env.exemple` como referência).
2. Suba o PostgreSQL localmente.
3. Inicie a aplicação:

```bash
./gradlew bootRun
```

A API ficará disponível em `http://localhost:8080/api/v1`.

## Executar com Docker Compose
```bash
docker compose up --build
```

Serviços:
- `backend`: `http://localhost:8080/api/v1`
- `postgres`: `localhost:5432`

## Variáveis de ambiente principais
- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_SCHEMA`
- `DB_USER`
- `DB_PASSWORD`
- `APP_CORS_ALLOWED_ORIGINS` (opcional)
- `JWT_SECRET` (opcional)
- `JWT_EXP_MINUTES` (opcional)

## Qualidade e testes
```bash
./gradlew test
./gradlew spotlessCheck
```

## Documentação
- [Roadmap](docs/ROADMAP.md)
- [Karma Spec](docs/KARMA-SPEC.md)
- [Projects Spec](docs/PROJECTS-SPEC.md)
- [Fixes](docs/fixes.md)
