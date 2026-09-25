# Copilot instructions — SDLC agent team

This file is loaded into every Copilot request in this repo. Role-specific process lives in
`.github/agents/*.agent.md`; keep shared conventions here so they aren't repeated in every agent.

## What this repo does

You give Copilot plain-language requirements. The `sdlc-orchestrator` agent runs a team of
specialist subagents that takes those requirements all the way to a pull request:

```
requirements ─▶ requirements-analyst ─▶ [HUMAN APPROVAL] ─▶ jira-story-writer
             ─▶ solution-architect ─▶ backend-dev / frontend-dev ─▶ test-writer
             ─▶ code-reviewer ─▶ security-reviewer ─▶ git-pr-agent ─▶ PR + Jira updated
                      ▲                    │
                      └── fix loop (max 2) ┘   (Blocker/Critical findings go back to the dev agent)
```

Bugs take a shorter path: `requirements-analyst → jira-story-writer → bug-fixer → test-writer →
code-reviewer → security-reviewer → git-pr-agent`.

Entry points (slash commands in `.github/prompts/`):

- `/sdlc <requirements>`: full run starting from raw requirements.
- `/sdlc-from-jira <KEY>[, <KEY>...]`: full run for issues that already exist in Jira (skips
  analysis and story writing).

## Jira

- Site: `https://ayushgupta9297.atlassian.net` (cloudId `8da3e0f5-cf13-429e-bf58-28c029b3183b`)
- Project: **`TAS`** (Todo Application system). Team-managed project.
- Issue types: `Epic`, `Story`, `Bug`, `Task`, `Subtask`. Stories are linked to their epic
  through the **`parent`** field (team-managed projects have no "Epic Link" field).
- MCP server name: `atlassian` (see `.vscode/mcp.json`). Agents reference individual tools as
  `atlassian/<toolName>` so each one gets only the Jira operations it needs.

## Tech stack (default — edit this section to change it for every agent)

The repo starts empty. The first run scaffolds the application with this stack:

- **Backend** (`backend/`): Java 21, Spring Boot 3.x, Maven wrapper (`./mvnw`). Layers are
  `controller -> service (interface + impl) -> repository (Spring Data JPA)`. DTOs are records,
  errors are handled in one `@RestControllerAdvice`, H2 for local dev.
  Tests use JUnit 5, Mockito and MockMvc.
  Packaged as an executable **WAR** so it can serve JSP.
- **Frontend** (inside `backend/`, no separate app): a **single-page application served by JSP**.
  One JSP shell (`src/main/webapp/WEB-INF/jsp/index.jsp`) with view markup in `<template>`
  fragments, vanilla JavaScript ES modules + CSS in `src/main/resources/static/` (hash router,
  `api/` client, `views/`, `components/`), and a `web/SpaController` that forwards `/` to the
  shell. No framework, no build step, no Node/npm. Tests: `@WebMvcTest` for the controller and
  Playwright for Java UI tests. `frontend-dev` owns these paths; `backend-dev` owns the rest of
  `backend/`.

Path-scoped rules are in `.github/instructions/` and applied automatically by glob.

## Run artifacts: `.sdlc/runs/<run-id>/`

Subagents do **not** share a conversation. They hand off through files. Every run gets its own
folder, created by the orchestrator:

| File | Written by | Contents |
|---|---|---|
| `run.md` | orchestrator | Run status tracker: stage checklist, Jira keys, branch, PR URL, decisions |
| `00-requirements.md` | orchestrator | The user's raw requirements, verbatim |
| `01-stories.md` | requirements-analyst, then jira-story-writer | Epic + stories with acceptance criteria; Jira keys are added once created |
| `02-design.md` | solution-architect | Affected modules, API contract, data model, step sequence, risks |
| `03-implementation.md` | dev agents / bug-fixer | Files changed per story, commands run and their results |
| `04-tests.md` | test-writer | AC → test mapping, test results, coverage gaps |
| `05-code-review.md` | code-reviewer | Findings by severity + verdict |
| `06-security-review.md` | security-reviewer | Findings by severity + verdict |
| `07-pr.md` | git-pr-agent | Branch, commits, PR URL, Jira updates made |

The `<run-id>` is `YYYY-MM-DD-<short-kebab-slug>` (e.g. `2026-09-23-task-due-dates`).
Agents append to files and never delete other agents' sections.

## Non-negotiables (all agents)

- **Never invent requirements.** Anything ambiguous goes under **Open Questions** / **Risks**
  and is surfaced to the human. Do not silently resolve it.
- **Stay in your lane.** Each agent does only its own stage. Reviewers never edit code.
  Planning agents write only inside `.sdlc/`.
- **Report truthfully.** Report only commands that were actually run and their actual output.
  "Tests pass" means you ran them and they passed.
- **Git hygiene.** Never commit to `main`. Name branches `<EPIC-or-STORY-KEY>-short-slug`, use
  Conventional Commits that reference the Jira key (`feat(TAS-12): ...`), and never force-push.
- **Backend:** constructor injection only, strict DTO/entity separation, no N+1 queries.
- **Frontend:** stays a single page (one JSP, client-side routing, data only via the REST API),
  no JSP scriptlets, no `innerHTML` with data, cover loading/empty/error/success states,
  accessibility (semantic HTML, keyboard access, labels, ARIA) is required.
- Match existing file/package conventions exactly once code exists.
