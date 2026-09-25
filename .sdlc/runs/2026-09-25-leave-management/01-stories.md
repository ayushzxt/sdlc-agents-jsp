# Stories — 2026-09-25-leave-management

## Epic
**Title**: Leave Request Management
**Goal**: Enable employees to submit and track leave requests, and enable an authorized approver to make a decision on those requests. The scope is intentionally limited because the raw requirement does not define leave policies, roles, or supporting workflows.
**Jira**: [LM-1](https://ayushgupta9297.atlassian.net/browse/LM-1)

## Stories

### S1 — Submit a leave request
**Type**: Story
**Priority**: High
**Size**: M
**Jira**: [LM-2](https://ayushgupta9297.atlassian.net/browse/LM-2)

**User story**: As an employee, I want to submit a leave request with the required leave details, so that my absence can be considered by the responsible approver.

**Acceptance criteria**
- [ ] AC1 — Given I am an authenticated employee and the leave request form is available, when I provide the required leave details and submit the form, then the application creates one leave request and shows a confirmation with its current status.
- [ ] AC2 — Given one or more required leave details are missing or invalid, when I submit the form, then the application does not create a leave request and identifies the fields that need correction.
- [ ] AC3 — Given the submitted date range is not acceptable according to the rules configured for the application, when I submit the form, then the application rejects the request with a clear validation message and does not create it.

**Notes**: The exact required fields and date-validation rules are unresolved; see Open Questions Q1–Q3.

### S2 — View submitted leave requests and statuses
**Type**: Story
**Priority**: High
**Size**: S
**Jira**: [LM-4](https://ayushgupta9297.atlassian.net/browse/LM-4)

**User story**: As an employee, I want to view my submitted leave requests and their statuses, so that I know whether each request is awaiting a decision or has been decided.

**Acceptance criteria**
- [ ] AC1 — Given I have no submitted leave requests, when I open my leave requests, then the application shows an empty state explaining that no requests are available.
- [ ] AC2 — Given I have submitted leave requests, when I open my leave requests, then the application shows each request with enough leave details to identify it and its current status.
- [ ] AC3 — Given another employee has submitted a leave request, when I open my leave requests, then that request is not visible to me unless an explicitly authorized role permits access.

**Notes**: The status vocabulary and access model require confirmation; see Open Questions Q4–Q5.

### S3 — Review and decide on a leave request
**Type**: Story
**Priority**: High
**Size**: M
**Jira**: [LM-3](https://ayushgupta9297.atlassian.net/browse/LM-3)

**User story**: As an authorized leave approver, I want to review a submitted leave request and approve or reject it, so that the request reaches a recorded decision.

**Acceptance criteria**
- [ ] AC1 — Given an authorized approver has pending leave requests, when the approver opens the pending requests view, then the application shows the requests available for that approver to review.
- [ ] AC2 — Given an authorized approver is viewing a pending leave request, when the approver approves it, then the application records the decision and the request status changes to approved.
- [ ] AC3 — Given an authorized approver is viewing a pending leave request, when the approver rejects it, then the application records the decision and the request status changes to rejected.
- [ ] AC4 — Given a leave request is no longer pending, when an approver attempts to approve or reject it, then the application prevents a second decision and leaves the existing decision unchanged.
- [ ] AC5 — Given a user is not authorized to approve leave requests, when that user attempts to access the approval action, then the application denies the action and does not change the request.

**Notes**: The approver role, decision comments, and notification behavior are unresolved; see Open Questions Q5–Q7.

## Non-functional requirements
- The application must prevent unauthorized users from viewing or changing another employee’s leave requests.
- Validation failures and authorization failures must be communicated in a clear, user-visible way.
- The leave request status and approval decision must remain consistent after the user receives confirmation of the operation.

## Out of scope
- Leave balance accrual, entitlement calculation, and balance adjustments.
- Payroll, attendance, calendar, or HR-system integrations.
- Email, SMS, or push notifications.
- Administrative configuration of leave types, holidays, policies, or approver hierarchies.
- Request cancellation, editing after submission, and appeals.

## Open Questions
- Q1 — Which roles and user types exist, and how does a user authenticate? This affects S1–S3.
- Q2 — What fields are required on a leave request (for example, leave type, start date, end date, partial-day selection, reason, or attachment)? This affects S1.
- Q3 — What date and overlap rules apply, including whether same-day leave, weekends, holidays, past dates, and overlapping requests are allowed? This affects S1.
- Q4 — Which statuses are required beyond pending, approved, and rejected, and what status should a newly submitted request receive? This affects S1–S2.
- Q5 — Who is authorized to approve a request: a direct manager, an HR role, a configured approver, or another role? What requests can each approver access? This affects S2–S3.
- Q6 — Is a rejection comment required, optional, or not supported? This affects S3.
- Q7 — Should employees be notified when a request is approved or rejected, and through which channel? This affects S3 and may create a follow-up story.
- Q8 — Can an employee edit or cancel a request, and if so, at which points in its lifecycle? This affects the scope of S1–S2.
- Q9 — Are leave balances or policy enforcement required for the first release, or should requests be accepted without balance validation? This affects S1 and the Out of scope list.
- Q10 — Are audit history, reporting, accessibility targets, performance targets, or data-retention requirements required? This affects all stories and the non-functional requirements.
