# Agents

## Purpose
- Define the actors ("agents") of OrionTask and their responsibilities.
- Keep ownership clear and interactions simple (KISS).

## Scope
- Human and software actors involved in the MVP.
- Current behavior and near-future additions that do not add complexity.

## Principles
- Simplicity first; fewer agents, stronger rules.
- One clear owner per responsibility.
- UI communicates rules; backend enforces them.

## Current Actors
- User (Human)
  - Creates Dharmas and Tasks.
  - Acts in "Agora" with max 5 tasks visible.

- Web Client (Frontend)
  - Renders UI (React + Tailwind + Vite).
  - Routes: `/login`, `/dharmas`, `/tasks/:dharmaId`, `/agora`.
  - Enforces UI limits (show ≤5 in Agora, indicate hidden, concise texts).
  - Stores: `userId`, `theme`, `showHidden` in localStorage.
  - Calls REST API at `/api/v1`.

- API Service (Backend)
  - Spring Boot 3 + PostgreSQL.
  - Enforces domain rules:
    - ≤8 Dharmas per user.
    - ≤5 tasks in `NOW` per user.
    - DONE tasks cannot be edited or deleted.
    - Dharma delete blocked if active tasks exist.
    - Hidden in Dharma propagates to Tasks.
  - Exposes controllers for Auth, Users, Dharmas, Tasks.

- Database (PostgreSQL)
  - Persists Users, Dharmas, Tasks and timestamps.
  - Applies schema via Flyway migrations.

## Core Interactions
- Users
  - Create user (signup minimal in MVP).
  - Retrieve user by id/username.
- Dharmas
  - List by user (with/without hidden).
  - Create, update, toggle hidden (propagates), delete with guard.
- Tasks
  - Create under Dharma (inherits hidden).
  - Edit when not DONE.
  - Move to `NOW`/change status with 5-limit guard.
  - Mark as DONE; cannot edit/delete after done.
  - List by Dharma or by user+status.
- Agora Flow (UI)
  - Load `NOW`; if <5, promote oldest `NEXT` to fill up to 5.

## Error and Feedback Channels
- Backend returns concise errors (English).
- Frontend maps to friendly toasts/messages, examples:
  - "Maximum of 5 tasks in NOW reached"
  - "Maximum of 8 dharmas reached"
  - "Completed tasks cannot be edited/deleted"
  - "Cannot delete dharma with active tasks"
  - "User not found / invalid credentials"

## Minimal Future Additions (Non-disruptive)
- Auth Agent (JWT)
  - Adds login with password, issues short-lived JWT.
  - Protects API, injects `userId` from token.
- Rule Guard (Tests)
  - Unit tests for services to freeze business rules.

## Non-Goals
- No microservices or queues.
- No push notifications or offline mode.
- No priorities, dates, or sub-tasks.
- No analytics-heavy tracking.
