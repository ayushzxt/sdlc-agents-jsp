## Backend — LM-2, LM-4, LM-3 (2026-09-25, iteration 1)
- Files: `backend/pom.xml`, Maven wrapper files, application configuration, entity/repository/dto/exception/security/service/controller/notification packages, and focused backend tests — implemented the Java 21 Spring Boot 3.5.5 WAR backend with BCrypt session authentication, Employee/Approver roles, unique active approver assignment, CSRF, leave lifecycle and validation, attachment metadata/storage, scoped queries, audit history, post-commit notification boundary, consistent API errors, and service/context tests.
- Commands: `./mvnw.cmd -q compile` -> passed.
- Commands: `./mvnw.cmd -q test` -> passed; 5 tests, 0 failures, 0 errors.
- Deviations from design: Frontend JSP/static files and the frontend-owned web package were not modified, per backend-dev scope. Playwright/UI tests and frontend artifacts remain for frontend-dev/test-writer stages. The local runtime reported Java 23.0.1; the Maven project source/target contract is Java 21.
- Findings addressed (fix mode): none.

## Frontend — LM-2, LM-4, LM-3 (2026-09-25, iteration 1)
- Files: `backend/src/main/webapp/WEB-INF/jsp/index.jsp` and `templates/*.jspf` (new) — single JSP shell, login, request, and approval templates with JSTL-safe asset URLs.
- Files: `backend/src/main/resources/static/js/` (new) — session-aware router, login/auth client, CSRF/error-aware API client, employee request create/list/detail/history/edit/cancel flows, attachment validation, approver filters and decision controls, and safe DOM components.
- Files: `backend/src/main/resources/static/css/app.css` (new) — responsive accessible visual system, request states, forms, status badges, decision panel, and audit timeline.
- Files: `backend/src/main/java/com/example/leavemanagement/web/SpaController.java` and `backend/src/test/java/com/example/leavemanagement/web/SpaControllerTest.java` (new) — JSP shell forwarding and focused MVC coverage.
- Files: `backend/src/main/resources/application.yml` (changed) — JSP view prefix/suffix configuration.
- Routes added/changed: `#/login`, `#/leave-requests`, `#/leave-requests/new`, `#/leave-requests/:id`, `#/leave-requests/:id/edit`, `#/leave-requests/:id/history`, `#/approvals`, and `#/approvals/:id`.
- Commands: `node --check` over all static JS -> passed; `./mvnw.cmd -q -Dtest=SpaControllerTest test` -> passed; `./mvnw.cmd -q verify` -> passed; runtime smoke with `./mvnw.cmd -q spring-boot:run`, `GET /` -> 200 with SPA mount, `GET /api/auth/csrf` -> 200 with token; manual browser/Playwright interaction check -> not done because Playwright UI tests and browser dependency were not present in the existing Maven project.
- Deviations from design: frontend files are consolidated under `static/css/app.css` because no prior CSS build or per-view stylesheet structure existed; no production API mocking was added.
- Findings addressed (fix mode): none.
