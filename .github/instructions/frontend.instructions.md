---
applyTo: "backend/src/main/webapp/**,backend/src/main/resources/static/**,backend/src/main/java/**/web/**,backend/src/test/java/**/web/**"
---

# Frontend conventions (JSP single-page app / vanilla JS)

Applied automatically to the frontend files inside `backend/`, on top of `copilot-instructions.md`.

- **One JSP page** (`WEB-INF/jsp/index.jsp`) is the app shell. Screens are client-side views
  switched by the hash router in `static/js/router.js`. No full-page reloads, no server-side
  navigation, no form posts to the server.
- `web/SpaController` only forwards `/` to the `index` view. JSP never loads view data; all data
  comes from the REST API via JavaScript.
- **JSP:** no scriptlets. JSTL + EL only, escape every dynamic value with `<c:out>` /
  `fn:escapeXml`, build URLs with `<c:url>`. View markup lives in `<template id="tpl-...">` in
  `WEB-INF/jsp/templates/<view>.jspf`.
- **JS:** vanilla ES modules, no framework, no build step, no npm. JSDoc types on public functions
  and `@typedef` for API shapes. Views export `mount(root, params)` / `unmount()`; `unmount`
  removes listeners and aborts in-flight requests.
- All HTTP calls go through `static/js/api/client.js` (JSON, CSRF header, `ApiError` on non-2xx).
  Views and components never call `fetch` directly.
- **XSS:** data goes into the DOM only with `textContent` / `value` / safe attributes. Never
  `innerHTML` (or similar) with data, and no inline event handlers.
- Every data-fetching or mutating view handles **loading, empty, error (`role="alert"`), and
  success** states.
- **Accessibility:** semantic HTML, a `<label>` for every input, full keyboard operability with
  visible focus, `aria-live` for async status, and on route change update `document.title` and
  focus the view's `<h1>`.
- Responsive layout with no fixed pixel widths that break narrow viewports. Reuse existing CSS
  custom properties; one `.css` file per view in `static/css/`.
- **Tests:** `@WebMvcTest` for `SpaController` (view name `index`), and Playwright for Java UI tests
  in `src/test/java/<base>/web/ui/` against `@SpringBootTest(webEnvironment = RANDOM_PORT)`. Stub
  the API with `page.route("**/api/**", ...)` for loading/empty/error states, and query by
  role/label/text (`getByRole`, `getByLabel`).
- Done means `./mvnw -q verify` passes from `backend/`.
