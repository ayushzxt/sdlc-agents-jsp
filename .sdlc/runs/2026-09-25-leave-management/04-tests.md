# Tests — 2026-09-25-leave-management (iteration 1)

## Traceability
| Story | AC / clarified rule | Test(s) | Result |
|---|---|---|---|
| LM-2 | AC1: authenticated employee submission creates `PENDING` request with confirmation data | `LeaveRequestServiceImplTest#createsPendingRequestWithServerOwnedApproverAuditAndNotification` | pass |
| LM-2 | AC2: required fields are rejected without persistence | `LeaveRequestServiceImplTest#rejectsMissingRequiredFieldsAndDoesNotPersist` | pass |
| LM-2 | AC3: invalid date range is rejected without persistence | `LeaveRequestServiceImplTest#acceptsSameDayLeaveAndRejectsReversedDates` | pass |
| LM-2 | BCrypt password verification and role mapping | `SecurityConfigTest#userDetailsUsesBcryptHashAndRoleAuthority` | pass |
| LM-2 | CSRF/session security boundary | Not covered by executable test; see gaps | gap |
| LM-2 | Past-date rejection | `LeaveRequestServiceImplTest#rejectsPastDate` | pass |
| LM-2 | PENDING/APPROVED overlap rejection | `LeaveRequestServiceImplTest#rejectsOverlap` | pass |
| LM-2 | Attachment PDF/JPG/PNG and size constraints | `LeaveRequestServiceImplTest#rejectsUnsupportedAndOversizedAttachments` | pass |
| LM-2 | Server-owned employee and assigned approver; approver cannot submit as employee | `LeaveRequestServiceImplTest#createsPendingRequestWithServerOwnedApproverAuditAndNotification`, `#rejectsApproverFromSubmittingAsEmployee` | pass |
| LM-2 | Submission audit and notification boundary | `LeaveRequestServiceImplTest#createsPendingRequestWithServerOwnedApproverAuditAndNotification` | pass |
| LM-4 | AC1: empty employee list returns empty result | `LeaveRequestServiceImplTest#listsEmployeesOwnRequestsAndRejectsEmployeeFilters` | pass |
| LM-4 | AC2: employee list contains identifying details and status | `LeaveRequestServiceImplTest#listsEmployeesOwnRequestsAndRejectsEmployeeFilters` | pass |
| LM-4 | AC3: employee-only visibility and scoped detail/history | `LeaveRequestServiceImplTest#listsEmployeesOwnRequestsAndRejectsEmployeeFilters`, `#usesEmployeeScopedRepositoriesForDetailAndHistory` | pass |
| LM-4 | Approver assigned-request/status/date scoping | `LeaveRequestServiceImplTest#listsApproverAssignedStatusesAndFiltersInvalidDateRange` | pass |
| LM-4 | Audit history access and chronological repository contract | `LeaveRequestServiceImplTest#usesEmployeeScopedRepositoriesForDetailAndHistory` | pass |
| LM-4 | PENDING-only edit; APPROVED/PENDING cancellation; REJECTED immutability | `LeaveRequestServiceImplTest#updatesOnlyPendingAndCancelsPendingOrApprovedButNotTerminalRequests` | pass |
| LM-4 | Lifecycle audit/notification boundary for edit/cancel | `LeaveRequestServiceImplTest#updatesOnlyPendingAndCancelsPendingOrApprovedButNotTerminalRequests` | pass |
| LM-3 | AC1: assigned approver can list pending requests | `LeaveRequestServiceImplTest#listsApproverAssignedStatusesAndFiltersInvalidDateRange` | pass |
| LM-3 | AC2: approve changes status and records optional comment/decision actor | `LeaveRequestServiceImplTest#approvesPendingRequestWithOptionalCommentAndRecordsDecision` | pass |
| LM-3 | AC3: reject requires comment, changes status, and notifies | `LeaveRequestServiceImplTest#rejectsPendingRequestWithRequiredCommentAndNotifies`, existing `#rejectsBlankRejectionComment` | pass |
| LM-3 | AC4: second decision is prevented and existing decision remains | `LeaveRequestServiceImplTest#preventsUnauthorizedAndSecondApprovalDecisions` | pass |
| LM-3 | AC5: unauthorized employee cannot approve | `LeaveRequestServiceImplTest#preventsUnauthorizedAndSecondApprovalDecisions` | pass |
| LM-3 | Assigned-approver scoping and decision audit/notification | `LeaveRequestServiceImplTest#approvesPendingRequestWithOptionalCommentAndRecordsDecision`, `#rejectsPendingRequestWithRequiredCommentAndNotifies` | pass |
| LM-3 | Approval HTTP contract: optional approval comment and required rejection comment/field error | `ApprovalControllerTest#approvesWithOptionalCommentAtHttpBoundary`, `#rejectsMissingOrBlankCommentBeforeCallingService` | pass |
| LM-3 | CSRF and HTTP status/JSON error contracts | Not covered by executable test; see gaps | gap |
| LM-2/LM-4/LM-3 | SPA loading, empty, validation, API-error, success, history, and decision states | Playwright Java not available in `pom.xml` and no browser dependency/setup was present; `SpaControllerTest` covers root and `/index` shell forwarding only | gap |

## Runs
- `cd backend && .\\mvnw.cmd -q "-Dtest=LeaveRequestServiceImplTest,SecurityConfigTest,SpaControllerTest,ApprovalControllerTest" test` -> 20 tests, 0 failures, 0 errors, 0 skipped.
- `cd backend && .\\mvnw.cmd -q verify` -> 21 tests, 0 failures, 0 errors, 0 skipped.
  - `ApprovalControllerTest`: 2
  - `LeaveManagementApplicationTests`: 1
  - `LeaveRequestServiceImplTest`: 15
  - `SecurityConfigTest`: 1
  - `SpaControllerTest`: 2

## Defects found
- None found by the executed tests.

## Coverage gaps not closed
- No Playwright Java dependency or installed browser setup exists, so browser-only SPA state, accessibility, keyboard, focus, hash navigation, and API-stubbing flows were not executable.
- No MockMvc security/controller slice tests were added for login/logout/session cookies, CSRF `403`, unauthorized `401`, multipart `201/400/409`, approval `200/400/403/404/409`, or normalized JSON error bodies.
- Service tests use Mockito repository boundaries; they do not provide a JPA integration assertion for the overlap query indexes or optimistic concurrent decision race.
- The focused test output includes non-failing Mockito/Java agent warnings under the local Java 23 runtime; the project source/target contract remains Java 21.
