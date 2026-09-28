# Design — 2026-09-25-leave-management

## Overview
The repository is empty, so implementation starts with a Spring Boot WAR under `backend/` and a vanilla JavaScript SPA served by one JSP shell. Authentication is session-based username/password with BCrypt hashes; Spring Security establishes the session and derives the current user for every operation. Employees can access only their own requests, while an `APPROVER` can access requests whose employee assignment points to that approver. Leave requests use the lifecycle `PENDING`, `APPROVED`, `REJECTED`, `CANCELLED`, with server-side date/overlap validation, audit events, and email notifications around each lifecycle operation.

## Scaffolding
- Generate a Maven project in `backend/` with Java 21, Spring Boot 3.5.x, WAR packaging, and the Spring Boot Maven plugin.
- Include `spring-boot-starter-web`, `spring-boot-starter-data-jpa`, `spring-boot-starter-validation`, `spring-boot-starter-security`, H2 runtime, `spring-boot-starter-mail`, `tomcat-embed-jasper`, JSTL API/implementation, and `spring-boot-starter-test`.
- Configure Playwright Java 1.55.x for UI tests; document or automate browser installation. Keep the SPA free of Node/npm and build tooling.
- Create `controller`, `service`, `service/impl`, `repository`, `entity`, `dto`, `exception`, `security`, `notification`, and `web` packages below `backend/src/main/java/<base-package>/`.
- Create `backend/src/main/webapp/WEB-INF/jsp/index.jsp`, JSPF templates under `WEB-INF/jsp/templates/`, JavaScript under `src/main/resources/static/js/`, and CSS under `src/main/resources/static/css/`.
- `web/SpaController` forwards `/` to the JSP shell. Security permits `/`, `/index`, static assets, login, and logout; `/api/**` requires authentication except `POST /api/auth/login`. CSRF protection remains enabled for session mutations, with the token exposed to the SPA through a safe response/header mechanism. Do not add CORS or a proxy because UI and API share one origin.
- Use H2 only for local development/tests. Do not seed passwords in source; test fixtures must create BCrypt hashes through the configured encoder.

## Affected Modules
| Story | Layer | File (new/changed) | Change |
|---|---|---|---|
| LM-2 | backend | `security/SecurityConfig.java`, `security/PasswordConfig.java`, `security/CurrentUserService.java`, `security/SessionUser.java` (new) | Configure form-independent JSON login/logout, session authentication, BCrypt, role mapping, CSRF, and current-user access. |
| LM-2 | backend | `entity/User.java`, `entity/Role.java`, `entity/ApproverAssignment.java`, corresponding repositories (new) | Store username, BCrypt password hash, Employee/Approver role, and exactly one active approver assignment per employee. |
| LM-2 | backend | `entity/LeaveRequest.java`, `entity/LeaveRequestStatus.java`, `entity/PartialDay.java`, `entity/Attachment.java` (new) | Model required leave data, optional partial day and PDF/JPG/PNG attachment, lifecycle, ownership, and decision metadata. |
| LM-2/LM-4 | backend | `dto/LeaveRequestCreateRequest.java`, `dto/LeaveRequestUpdateRequest.java`, `dto/LeaveRequestResponse.java`, `dto/AttachmentResponse.java` (new) | Validate and expose leave data without exposing entities, password hashes, or attachment bytes in list responses. |
| LM-2/LM-4/LM-3 | backend | `repository/LeaveRequestRepository.java`, `repository/AuditEventRepository.java` (new) | Query by employee, assigned approver, status, and date range; detect PENDING/APPROVED overlap; preserve indexed list/history access. |
| LM-2/LM-4/LM-3 | backend | `service/LeaveRequestService.java`, `service/impl/LeaveRequestServiceImpl.java`, `service/AuditService.java` (new) | Enforce ownership, assigned-approver authorization, lifecycle rules, date/overlap rules, audit creation, and transactions. |
| LM-2/LM-4/LM-3 | backend | `controller/AuthController.java`, `controller/LeaveRequestController.java`, `controller/ApprovalController.java`, `controller/ApiExceptionHandler.java` (new) | Implement authentication, employee request/list/history operations, approver review/decision operations, and consistent errors. |
| LM-2/LM-4/LM-3 | backend | `notification/LeaveNotificationService.java`, `notification/EmailLeaveNotificationService.java` (new) | Send email notifications after submit, approve, reject, and cancel using configured mail delivery; never include credentials or attachment bytes unnecessarily. |
| LM-2/LM-4/LM-3 | frontend | `WEB-INF/jsp/index.jsp`, `templates/leave-requests.jspf`, `templates/approvals.jspf`, `templates/login.jspf` (new) | Provide the single SPA shell and semantic templates for authentication, employee requests/history, approvals, and audit history. |
| LM-2/LM-4/LM-3 | frontend | `static/js/api/client.js`, `api/auth.js`, `api/leave-requests.js`, `router.js`, `views/login.view.js`, `views/leave-requests.view.js`, `views/approvals.view.js`, `components/leave-request-form.js`, `components/leave-request-list.js`, `components/audit-history.js` (new) | Implement session-aware API calls, create/edit/cancel forms, employee and approver views, history, attachment upload, and all loading/empty/error/success states. |
| LM-2/LM-4/LM-3 | frontend | `static/css/base.css`, `leave-requests.css`, `approvals.css` (new) | Responsive accessible forms, tables/lists, status and validation presentation, and visible focus treatment. |
| LM-2/LM-4/LM-3 | backend/frontend tests | `backend/src/test/java/<base-package>/**` (new) | Add security, service, repository, MockMvc, SPA controller, and Playwright coverage for every acceptance criterion and resolved rule. |

## API Contract
All JSON errors use `{ "code": string, "message": string, "fieldErrors": [{ "field": string, "message": string }] }`; no stack traces, password hashes, or cross-user existence details are returned. Dates are `YYYY-MM-DD`, timestamps are ISO-8601 instants, and multipart requests use `multipart/form-data`.

### `POST /api/auth/login`
Request: `{ "username": string, "password": string }`.
Success: `200 CurrentUserResponse` with username, roles, and CSRF token metadata; establishes an authenticated HTTP session.
Errors: `400 ApiError` for malformed input; `401 ApiError` for invalid credentials without revealing which credential failed; `429 ApiError` if the configured login-throttling boundary is exceeded.

### `POST /api/auth/logout`
Requires an authenticated session and CSRF token.
Success: `204` and invalidates the session. Errors: `401 ApiError` when unauthenticated; `403 ApiError` for a missing/invalid CSRF token.

### `GET /api/auth/me`
Success: `200 CurrentUserResponse` for the current session. Errors: `401 ApiError` when unauthenticated.

### `POST /api/leave-requests`
Requires an authenticated `EMPLOYEE`; derives the employee and assigned approver from the session/server data. Multipart fields: `leaveType` string 1–50, `startDate`, `endDate`, `reason` string 1–1000, optional `partialDay` (`FULL_DAY`, `FIRST_HALF`, `SECOND_HALF`), and optional `attachment` limited to PDF/JPG/PNG and a configured size limit.
Success: `201 LeaveRequestResponse` with `PENDING` status and `Location` header; sends a submission email.
Errors: `400 ApiError` for missing/invalid fields, unsupported partial-day value/type/size, or `endDate < startDate`; `401 ApiError`; `403 ApiError` for a non-employee role; `409 ApiError` for a past-date or overlap with the same employee’s PENDING/APPROVED request.

### `GET /api/leave-requests`
Requires authentication. Employees receive only their own requests, ordered newest first. Approvers receive only PENDING, APPROVED, or REJECTED requests for their assigned employees and may filter by `status` (`PENDING`, `APPROVED`, `REJECTED`) and date range.
Success: `200 { "items": LeaveRequestSummary[] }`, including an empty array when none match. Errors: `400 ApiError` for invalid filters; `401 ApiError`; `403 ApiError` if the role cannot access the listing.

### `GET /api/leave-requests/{id}`
Requires authentication and employee ownership or approver assignment.
Success: `200 LeaveRequestResponse`, including attachment metadata but not raw bytes. Errors: `401 ApiError`; `403 ApiError` or normalized `404 ApiError` for another employee’s request; `404 ApiError` when absent.

### `PATCH /api/leave-requests/{id}`
Requires the owning employee and `PENDING` status. Accepts the same editable fields as create; omitted attachment leaves it unchanged, while a supplied attachment replaces it.
Success: `200 LeaveRequestResponse` with `PENDING`; creates an `Updated` audit event and sends a notification. Errors: `400`, `401`, `403`, `404`, and `409` for non-PENDING state, past date, or overlap.

### `POST /api/leave-requests/{id}/cancel`
Requires the owning employee and status `PENDING` or `APPROVED`.
Success: `200 LeaveRequestResponse` with `CANCELLED`; creates a `Cancelled` audit event and sends a notification. Errors: `401`, `403`, `404`, and `409` for `REJECTED`/`CANCELLED` or a concurrent state change.

### `GET /api/leave-requests/{id}/history`
Requires the same visibility as the detail endpoint.
Success: `200 { "items": AuditEventResponse[] }` ordered oldest first, containing only `Created`, `Updated`, `Approved`, `Rejected`, and `Cancelled` events. Errors: `401`, `403`, and `404`.

### `POST /api/approvals/leave-requests/{id}/approve`
Requires role `APPROVER` and an assignment to the request’s employee; request body has optional `comment` string 0–1000.
Success: `200 LeaveRequestResponse` with `APPROVED`; creates an `Approved` audit event and sends a notification. Errors: `400`, `401`, `403`, `404`, and `409` if the request is not PENDING or another decision wins concurrently.

### `POST /api/approvals/leave-requests/{id}/reject`
Requires role `APPROVER` and assignment to the request’s employee; body `{ "comment": string (1–1000, required) }`.
Success: `200 LeaveRequestResponse` with `REJECTED`; creates a `Rejected` audit event and sends a notification. Errors: `400`, `401`, `403`, `404`, and `409` if the request is not PENDING or another decision wins concurrently.

## Data Model
- `users`: `id UUID`, unique `username`, required `passwordHash` produced only by BCrypt, enabled flag, and role `EMPLOYEE` or `APPROVER`.
- `approver_assignments`: unique `employee_id`, required `approver_id`, and active assignment metadata. The service rejects more than one active assignment for an employee.
- `leave_requests`: `id UUID`, `employee_id`, `approver_id` snapshot, `leave_type`, `start_date`, `end_date`, required `reason`, nullable `partial_day`, nullable attachment reference, `status`, `submitted_at`, `updated_at`, `decided_at`, `decided_by`, nullable decision comment, and optimistic `version`.
- `leave_attachments`: request ID, original filename, content type restricted to PDF/JPG/PNG, size, and binary content. Keep bytes out of list/detail DTOs; expose a separately authorized download endpoint only if the UI requires it.
- `audit_events`: request ID, event type, actor ID, event time, and optional comment. Retain indefinitely; do not store passwords or unnecessary sensitive data.
- Add indexes on `leave_requests(employee_id, status, start_date, end_date)`, `leave_requests(approver_id, status, start_date)`, and `audit_events(request_id, event_time)`. The overlap query is `existing.startDate <= new.endDate AND existing.endDate >= new.startDate` restricted to the same employee and PENDING/APPROVED statuses.
- The service rejects `startDate` before the current server date and reversed ranges. Same-day leave is valid. Weekends, holidays, balances, and working-day calculations are not evaluated.
- State transitions are guarded atomically: create -> PENDING; PENDING -> APPROVED/REJECTED/CANCELLED; APPROVED -> CANCELLED. REJECTED and CANCELLED are immutable. Editing is PENDING-only.

## Frontend
- Routes: `#/login`, `#/leave-requests`, `#/approvals`, and `#/leave-requests/:id/history`. The router checks `/api/auth/me`, redirects unauthenticated users to login, updates `document.title`, and focuses each view heading.
- The employee view provides required leave type, start/end dates, reason, optional partial-day selection and attachment input, plus edit/cancel actions enabled only by status. It renders loading, empty, validation, API-error, and success states.
- The approval view is available only to `APPROVER`, lists assigned employees’ pending and decided requests, and provides approve with optional comment and reject with required comment. Unauthorized navigation shows a forbidden state without leaking data.
- History is a basic chronological audit list; list filters support status and date range for approvers. No balance or policy administration UI is included.
- All API calls go through `client.js`, which sends cookies/CSRF headers, parses JSON, converts non-2xx responses to `ApiError`, and supports abort signals. Render API data with `textContent`, `value`, and safe attributes only; never use `innerHTML` with data. Use labels, keyboard-operable controls, visible focus, `role="alert"`, and `aria-live`.

## Sequence of Steps
1. [backend] Scaffold the Java 21 Spring Boot 3.5.x WAR, H2 profiles, JSP dependencies, mail configuration boundary, package structure, and `SpaController`.
2. [backend] Implement `User`, BCrypt session security, JSON login/logout/current-user endpoints, roles, CSRF handling, and the one-approver assignment repository/service.
3. [backend] Implement leave entities, attachment validation/storage, status transitions, indexed queries, overlap/past-date validation, DTOs, and audit persistence.
4. [backend] Implement employee create/list/detail/edit/cancel/history endpoints and approver list/approve/reject endpoints with ownership/assignment checks and conditional state updates.
5. [backend] Implement post-commit email notifications for submit/approve/reject/cancel and consistent exception mapping.
6. [frontend] Add the JSP shell/templates, session-aware router/login view, API client, employee form/list/history views, attachment and lifecycle controls, and accessible CSS.
7. [frontend] Add the approver list and decision views with role gating, required rejection comment, optional approval comment, and forbidden/conflict handling.
8. [backend] Add unit and repository tests for BCrypt/session identity, assignment scoping, date/overlap rules, all legal/illegal transitions, audit events, and notification dispatch.
9. [backend] Add MockMvc tests for every endpoint’s success, validation, authentication, authorization, not-found, conflict, multipart, and CSRF responses; add `@WebMvcTest` coverage for `SpaController`.
10. [frontend] Add Playwright Java tests with API routes stubbed for login, loading/empty/error/success, edit/cancel, attachment validation, history, and approver decisions.
11. [backend/frontend] Run `./mvnw -q verify` from `backend/`, including UI tests and browser prerequisites, and record actual results in `03-implementation.md` and `04-tests.md`.

## Test Strategy
- **LM-2:** service/repository tests cover BCrypt authentication, role checks, assigned-approver resolution, valid creation, required fields, attachment type/size, past dates, reversed ranges, same-day acceptance, PENDING/APPROVED overlap rejection, server-owned identity, audit creation, and submit email dispatch. MockMvc covers `201`, `400`, `401`, `403`, `409`, CSRF, and multipart responses. Playwright covers login, validation, confirmation, and attachment UI.
- **LM-4:** repository/service tests verify employee-only visibility, approver-assignment visibility, status/date filters, ordering, empty results, detail/history access, PENDING-only editing, PENDING/APPROVED cancellation, immutable rejected/cancelled requests, audit events, and notification dispatch. MockMvc verifies cross-employee denial and lifecycle conflicts. Playwright covers list, empty, history, edit, cancel, and error states.
- **LM-3:** service tests cover assigned-approver authorization, optional approval comments, required rejection comments, decision metadata, email dispatch, and atomic second-decision protection. MockMvc covers `200`, `400`, `401`, `403`, `404`, `409`, and CSRF. Playwright covers pending display, successful approval/rejection, required rejection comment, forbidden access, and stale/conflict feedback.
- Use fixed clocks and deterministic users in service tests. Use test principals with session cookies, never client-supplied employee/approver IDs. Add an indexed-query integration test and a representative normal API timing check against the local H2 profile; the target is under 500 ms, not a hard correctness gate in unit tests.
- Verify indefinite retention by confirming audit records are not deleted by lifecycle operations. Verify accessibility with semantic roles/labels, keyboard navigation, focus management, and screen-reader live regions.

## Risks
- Email delivery depends on configured SMTP/provider credentials and can be unavailable in local development; isolate it behind `LeaveNotificationService`, use a test implementation/profile, and record delivery failures without rolling back a successful leave transaction.
- Attachment bytes in H2/database storage may not scale for large files; enforce a configured size limit and keep the storage interface replaceable. A future object store can implement the same service without changing the request API.
- Session authentication requires secure cookie settings, CSRF protection, password-rate limiting, and no password/hash logging. These are implementation/security-review gates, not open product decisions.
- Concurrent edit/decision/cancel operations can race; optimistic version checks and conditional status updates must return `409` without overwriting the winning state.
- No leave balance, holiday/weekend calculation, payroll integration, or configurable policy administration is included; the stated date and overlap rules are the complete first-release validation boundary.

## Return To Orchestrator
Dispatch `backend-dev` first for scaffolding, session security, persistence, contracts, lifecycle services, audit, and notifications; then dispatch `frontend-dev` for the SPA flows against the stable API. `test-writer` follows both dev agents, then code and security review. No blocking product risks remain; SMTP and attachment storage are environment-specific implementation configuration, not product blockers.
