---
applyTo: "backend/src/main/webapp/**,backend/src/main/resources/static/**,backend/src/main/java/**/web/**,backend/src/test/java/**/web/**"
---

# Frontend conventions (JSP single-page app / vanilla JS)

Applied automatically to the frontend files inside `backend/`, on top of `copilot-instructions.md`.

- **One JSP page** (`WEB-INF/jsp/index.jsp`) is the app shell: header, nav, `<main id="app">`, an
  `aria-live` status region, and bootstrap config (context path, CSRF token, app version) in
  `<meta>` tags. Screens are client-side views switched by the hash router in
  `static/js/router.js`. Navigation uses `<a href="#/...">` and back/forward must work. No
  full-page reloads, no server-side navigation or redirects, no form posts to the server.
- `web/SpaController` only forwards `/` to the `index` view and never collides with `/api/**`.
  JSP never loads view data. All data comes from the REST API via JavaScript.
- **JSP:** no scriptlets. JSTL + EL only. EL in template text is **not** escaped by default, so
  escape every dynamic value with `<c:out>` / `fn:escapeXml`. Build URLs with `<c:url>`. `index.jsp`
  sets `<%@ page contentType="text/html;charset=UTF-8" %>`. View markup lives in
  `<template id="tpl-...">` in `WEB-INF/jsp/templates/<view>.jspf`. JS clones a template and fills
  it. It never builds HTML from strings that contain data.
- **JS:** vanilla ES2020+ modules, no framework, no jQuery, no build step, no npm. A third-party
  library needs a one-line justification in the design. JSDoc `@param` / `@returns` on public
  functions, and API shapes as `@typedef` in the matching `api/` module. Views export
  `mount(root, params)` / `unmount()`. `unmount` removes listeners, aborts in-flight requests
  (`AbortController`), and unsubscribes from the store.
- All HTTP calls go through `static/js/api/client.js`: JSON, the CSRF header from the `<meta>` tag
  on mutating requests, and a thrown `ApiError { status, message, fieldErrors }` on non-2xx. Views
  and components never call `fetch` directly. Forms submit through JS (`preventDefault()`).
- **XSS:** data goes into the DOM only with `textContent` / `value` / `setAttribute` on safe
  attributes. Never `innerHTML`, `outerHTML`, `insertAdjacentHTML` or `document.write` with data,
  and no inline event handlers (use `addEventListener`).
- Every data-fetching or mutating view handles **loading, empty, error (`role="alert"`), and
  success** states. Field errors from `ApiError.fieldErrors` are shown next to the field with
  `aria-describedby`.
- **Accessibility:** semantic HTML, a `<label>` for every input, full keyboard operability with
  visible focus, `aria-live` for async status. On route change, update `document.title` and focus
  the view's `<h1>`. Mark the active nav link with `aria-current="page"`.
- Responsive layout with no fixed pixel widths that break narrow viewports. Reuse existing CSS
  custom properties. Use one `.css` file per view in `static/css/`.
- **Tests:** `@WebMvcTest` for `SpaController` (view name `index`), and Playwright for Java UI tests
  in `src/test/java/<base>/web/ui/` against `@SpringBootTest(webEnvironment = RANDOM_PORT)`. Stub
  the API with `page.route("**/api/**", ...)` for loading/empty/error states, and query by
  role/label/text (`getByRole`, `getByLabel`).
- Done means `./mvnw -q verify` passes from `backend/`.
