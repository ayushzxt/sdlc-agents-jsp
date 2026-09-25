---
agent: sdlc-orchestrator
description: 'Run the SDLC pipeline for existing Jira issues (skips story writing): design -> code -> tests -> reviews -> PR.'
argument-hint: Jira keys, e.g. TAS-12, TAS-13
---

Run the SDLC pipeline in **jira-import mode** for these existing Jira issues:

${input:keys:Jira issue keys, comma-separated (e.g. TAS-12, TAS-13)}

Skip Stage 1 and Gate A. Have `jira-story-writer` import the issues into `01-stories.md`, then
continue from Stage 3 (Design), or go to `bug-fixer` for Bug issues. If any imported issue has no
acceptance criteria, stop and ask me before designing it.
