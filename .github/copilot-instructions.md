# Copilot instructions — SDLC agent team

This file is loaded into **every** Copilot request, including every subagent call, so keep it
short. Role-specific process lives in `.github/agents/*.agent.md`. Path-scoped code rules live
in `.github/instructions/*.instructions.md`.

## Pipeline

`sdlc-orchestrator` drives specialist subagents from requirements to a pull request with green
CI and a passing SonarQube Cloud quality gate (`/sdlc <requirements>` or
`/sdlc-from-jira <KEY>, ...`). Subagents share no conversation. They hand off through files in
`.sdlc/runs/<run-id>/` (`<run-id>` = `YYYY-MM-DD-<short-kebab-slug>`):

| File | Written by |
|---|---|
| `run.md`, `00-requirements.md` | orchestrator |
| `01-stories.md` | requirements-analyst, then jira-story-writer |
| `02-design.md` | solution-architect |
| `03-implementation.md` | backend-dev, frontend-dev, bug-fixer |
| `04-tests.md` | test-writer |
| `05-code-review.md` / `06-security-review.md` | code-reviewer / security-reviewer |
| `07-pr.md` | git-pr-agent |
| `08-ci.md` / `09-sonar.md` | ci-checker / sonar-reviewer |

Append to shared files. Never delete another agent's section.

## Tech stack

- **Backend** (`backend/`): Java 21, Spring Boot 3.x, Maven wrapper (`./mvnw`), executable WAR,
  H2 for local dev. `controller -> service (interface + impl) -> Spring Data JPA repository`.
  Tests: JUnit 5, Mockito, MockMvc.
- **Frontend** (inside `backend/`): a single-page app served by one JSP shell, with vanilla JS
  ES modules and CSS under `src/main/resources/static/`. No framework, no build step, no npm.
  UI tests use Playwright for Java. `frontend-dev` owns `src/main/webapp/**`,
  `src/main/resources/static/**` and `**/web/**`. `backend-dev` owns the rest of `backend/`.
- **Jira**: project `TAS` on `https://ayushgupta9297.atlassian.net`, MCP server `atlassian`.
- **CI**: GitHub Actions `.github/workflows/ci.yml` on GitHub-hosted runners runs `./mvnw verify`
  (JaCoCo coverage) and the SonarQube Cloud analysis. The quality gate must pass. Sonar keys are
  in `backend/pom.xml`.

## Non-negotiables (all agents)

- **Never invent requirements.** Ambiguity goes under Open Questions / Risks for the human.
- **Stay in your lane.** Do only your stage. Reviewers never edit code. Planning agents write
  only inside `.sdlc/`.
- **Report truthfully.** Report only commands you actually ran, with their actual results.
- **Git:** never commit to `main`, branch as `<KEY>-short-slug`, use Conventional Commits with
  the Jira key (`feat(TAS-12): ...`), never force-push.
- Match existing file and package conventions once code exists.

## Token discipline (all agents)

- Read only what your stage needs: your stories' sections of `01-stories.md`, the design
  sections for your layer, and the files you will touch. Use `search` to locate code before
  reading whole files.
- Never paste file contents, full diffs or full command output into artifacts or replies.
  Summarise: counts, failing test names, `file:line`, the first relevant error lines.
- Run Maven with `-q`. On failure, look at the failing tests' output, not the whole log.
- Your final reply to the orchestrator is **at most 15 lines** in the shape your Definition of
  done asks for. Details belong in your artifact file.
