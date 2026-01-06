# OrionTask

Simple, purpose-driven task manager (Dharma/Karma) designed for ADHD.

## Summary
- What: purpose-driven task manager (ADHD-first, KISS).
- Key rule: max 5 visible tasks in Agora.
- Stack: Spring Boot 3 + PostgreSQL; React 19 + TS + Vite + Tailwind + Zustand.
- API base: `/api/v1`.
- Start: backend `./gradlew bootRun`; frontend `npm run dev`.
- Sections: [English](#english) · [Português (PT-BR)](#português-pt-br)
- Apps: [backend/](backend) · [frontend/](frontend)
- Docs: [Overview](docs/00-visao-geral.md) · [Technical Architecture](docs/05-arquitetura-tecnica.md) · [Backend](docs/06-backend-spring.md) · [Frontend](docs/07-frontend.md) · [Agents](agents.md) · [Next steps](oriontask-next-steps.md) · [Current State](docs/10-estado-atual.md)
- API: [OpenAPI/Spec](OrionTask-API.json)

## English
- Focus: fewer choices, stronger rules (KISS). Max 5 visible tasks.
- Dharma: every task belongs to a life area (Health, Family, Learning).
- Karma: short, visible consequences motivate action.
- Tech: Spring Boot 3 + PostgreSQL (API at `/api/v1`), React 19 + TS + Vite + Tailwind + Zustand.

### What it does
- Dharmas: organize tasks by life areas.
- Karma: see gain/loss at a glance.
- Agora: up to 5 tasks for immediate action.

### Run locally
- Backend
```bash
cd backend
./gradlew bootRun
```
- Frontend (from `frontend/`)
```bash
npm install
npm run dev
```

### Not in scope (MVP)
- No sub-tasks, complex filters, or detailed agenda.
- No push notifications or offline mode.

Status: MVP under active development.

## Português (PT-BR)
- Foco: menos escolhas, regras fortes (KISS). Máx. 5 tarefas visíveis.
- Dharma: toda tarefa pertence a uma área de vida (Saúde, Família, Aprendizado).
- Karma: consequências curtas e visíveis para motivar ação.
- Tech: Spring Boot 3 + PostgreSQL (API em `/api/v1`), React 19 + TS + Vite + Tailwind + Zustand.

### O que faz
- Dharmas: organize tarefas por áreas de vida.
- Karma: veja ganho/perda rapidamente.
- Agora: até 5 tarefas para ação imediata.

### Como executar
- Backend
```bash
cd backend
./gradlew bootRun
```
- Frontend (em `frontend/`)
```bash
npm install
npm run dev
```

### Fora do escopo (MVP)
- Sem sub-tarefas, filtros complexos ou agenda detalhada.
- Sem notificações push ou modo offline.

Status: MVP em desenvolvimento.
