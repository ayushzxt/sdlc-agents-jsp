---
agent: sdlc-orchestrator
description: 'Run the full SDLC from plain-language requirements: Jira stories -> design -> code -> tests -> reviews -> PR.'
argument-hint: Describe what you want built (add "autopilot" to skip the story approval gate)
---

Run the complete SDLC pipeline for the following requirements, following your instructions in
`sdlc-orchestrator.agent.md`, starting at **Stage 1 (Analyse)**:

${input:requirements:Describe the feature or bug you want built}

Stop at Gate A for my approval of the stories before anything is created in Jira, unless the
requirements above say "autopilot". At the end, give me the Jira keys, the branch, and the PR URL.
