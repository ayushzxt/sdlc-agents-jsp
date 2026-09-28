# Run 2026-09-25-leave-management

**Mode**: requirements
**Epic**: [LM-1](https://ayushgupta9297.atlassian.net/browse/LM-1)
**Stories**: [LM-2](https://ayushgupta9297.atlassian.net/browse/LM-2), [LM-4](https://ayushgupta9297.atlassian.net/browse/LM-4), [LM-3](https://ayushgupta9297.atlassian.net/browse/LM-3)
**Branch**: pending
**PR**: pending

## Stages
- [x] 1 Analyse
- [x] A Human approval
- [x] 2 Jira stories created
- [x] 3 Design
- [x] 4 Build
- [x] 5 Tests
- [ ] 6 Code review (iteration: 0)
- [ ] 7 Security review (iteration: 0)
- [ ] 8 PR raised

## Decisions
- 2026-09-25 — Run started from raw requirement: create a leave management application.
- 2026-09-25 — Human approved the drafted epic and stories without changes.
- 2026-09-25 — Human decided: session-based username/password authentication with BCrypt; Employee and Approver roles; one assigned approver per employee; required leave type, dates, reason, and optional partial-day/attachment; no past or overlapping pending/approved requests; statuses PENDING/APPROVED/REJECTED/CANCELLED; required rejection comment; email notifications; pending edit/cancel and approved cancel; simple audit history; no balances; basic reporting/accessibility/performance; indefinite retention.

## Log
- 1 Analyse — requirements-analyst — drafted three stories covering submission, tracking, and approval; awaiting human approval.
- A Human approval — user — approved the drafted epic and stories.
- 2 Publish — jira-story-writer — created and verified epic LM-1 and stories LM-2, LM-4, and LM-3 in the Leave management project.
- 2 Publish — jira-story-writer — blocked because the configured Atlassian Jira MCP tools are unavailable; no Jira issues were created.
- 3 Design — solution-architect — design completed; implementation blocked pending authentication and approver authorization decisions.
- Design clarification — user — resolved authentication, authorization, leave rules, lifecycle, notifications, editing/cancellation, audit, and non-functional requirements.
- 4 Build — backend-dev and frontend-dev — implemented the Spring Boot backend and JSP vanilla JS SPA; compile, verify, JS syntax, and runtime smoke checks passed.
- 5 Tests — test-writer — added acceptance-criteria coverage; `./mvnw.cmd -q verify` passed with 21 tests and no failures.