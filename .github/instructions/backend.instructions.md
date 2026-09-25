---
applyTo: "backend/**"
---

# Backend conventions (Java 21 / Spring Boot 3)

Applied automatically to files under `backend/`, on top of `copilot-instructions.md`. The JSP
SPA files (`src/main/webapp/**`, `src/main/resources/static/**`, the `web` package) follow
`frontend.instructions.md` instead.

- **Layering:** `controller → service (interface in service/, impl in service/impl/) →
  repository (Spring Data JPA)`. Controllers never use repositories directly, and repositories
  hold no business logic.
- **DTOs are records** (`dto/`). Entities (`entity/`) never leave the service layer.
- **Constructor injection only.** No `@Autowired` field or setter injection.
- **Entities have behaviour methods** (e.g. `task.complete()`) instead of setters called from
  outside.
- **Validation:** `jakarta.validation` on request DTOs, `@Valid` on controller params.
- **Errors:** domain exceptions thrown from services, mapped in one `@RestControllerAdvice` to a
  consistent `ApiError` body. No stack traces in responses.
- **Transactions:** `@Transactional(readOnly = true)` on the service class, `@Transactional` on
  mutating methods.
- **No N+1 queries.** Use fetch joins or dedicated queries.
- **Logging:** SLF4J `LoggerFactory.getLogger(X.class)`. Never log secrets or PII.
- **Tests:** JUnit 5 + Mockito + AssertJ for services, `@WebMvcTest` + MockMvc for controllers,
  mirroring the main package structure.
- Done means `./mvnw -q compile && ./mvnw -q test` passes from `backend/`.
