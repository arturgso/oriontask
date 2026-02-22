# Implementation Plan: Karma Awareness System (Non-Gamified)

This document outlines the technical steps to implement the Karma system as a "Mirror of Action" based on Hindu philosophy and ADHD-friendly design.

## Core Philosophy
*   **Karma is Residue:** It is the record of energy spent, not a score to be "won."
*   **No Guilt:** No streaks, no "missed day" penalties, and no "leveling down."
*   **Awareness over Achievement:** The goal is to show the user *how* they are working, not *how much*.

---

## Phase 1: Database Migration (The Legacy)
**Goal:** Prepare the `tab_users` table to store the long-term distribution of energy.

1.  **Create Migration `V13__ADD_KARMA_COUNTERS.sql`:**
    *   `karma_action_total`: (Integer) Total volume of ACTION tasks.
    *   `karma_people_total`: (Integer) Total volume of PEOPLE tasks.
    *   `karma_thinking_total`: (Integer) Total volume of THINKING tasks.
    *   *Note:* Use "volume" (EffortLevel weights) rather than just "counts" to reflect true energy.

---

## Phase 2: Backend Logic (The Settling of Karma)
**Goal:** Automatically update the user's "Legacy" when a task is completed.

1.  **Define Effort Weights:**
    *   `LOW`: 1 unit
    *   `MEDIUM`: 3 units
    *   `HIGH`: 5 units
2.  **Update `TasksService.markAsDone`:**
    *   When a task status changes to `DONE`:
        1. Identify `KarmaType` (ACTION, PEOPLE, or THINKING).
        2. Identify `EffortLevel` weight.
        3. Increment the corresponding `karma_xxx_total` in the `Users` entity.
        4. Set `completedAt = current_timestamp`.

---

## Phase 3: The Awareness Engine (The Mirror)
**Goal:** Create a service to calculate the "Rolling Awareness" (Last 7 Days).

1.  **Create `KarmaService`:**
    *   **Method `getRollingSummary(userId)`:**
        1. Query all tasks for the User where `status = DONE` and `completedAt` is within the last 7 days.
        2. Sum the weights (EffortLevel) per `KarmaType`.
        3. Calculate percentages (e.g., 60% Action, 30% People, 10% Thinking).
2.  **Define "States of Being":**
    *   **The Striver (Sramana):** Predominant `ACTION` (> 50%).
    *   **The Connector (Sangha):** Predominant `PEOPLE` (> 50%).
    *   **The Seer (Rishi):** Predominant `THINKING` (> 50%).
    *   **The Balanced (Samatva):** No single type exceeds 50%.

---

## Phase 4: API & DTOs
**Goal:** Deliver the "Mirror" data to the frontend.

1.  **Create `KarmaSummaryDTO`:**
    ```json
    {
      "rolling_distribution": {
        "ACTION": 12,
        "PEOPLE": 4,
        "THINKING": 2
      },
      "current_state": "THE_STRIVER",
      "legacy_totals": {
        "ACTION": 450,
        "PEOPLE": 120,
        "THINKING": 80
      }
    }
    ```
2.  **Add Endpoint to `UsersController`:**
    *   `GET /users/me/karma`: Returns the `KarmaSummaryDTO`.

---

## Phase 5: ADHD Safety Checks (Validation)
1.  **Empty State:** If 0 tasks are completed in 7 days, the `current_state` should be `NEUTRAL` (no "You failed" messages).
2.  **Non-Attachment:** Ensure that deleting a `DONE` task does **not** decrement the user's historical counters (Karma, once settled, is part of the past).

---

## Implementation Steps for Developer
1. [ ] Run Migration `V13`.
2. [ ] Update `Users.java` entity with new fields.
3. [ ] Create `KarmaService.java` for calculations.
4. [ ] Modify `TasksService.markAsDone` to call `KarmaService.settleKarma()`.
5. [ ] Add `GET /users/me/karma` endpoint.
6. [ ] Write Unit Test: Verify "The Striver" state is correctly identified when Action tasks predominate.
