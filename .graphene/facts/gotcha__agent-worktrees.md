---
category: gotcha
subject: agent-worktrees
---

Agent tool isolation 'worktree' may create the worktree from an old commit (seen: 65dd718, the main merge) instead of the work branch HEAD, and without local.properties. Tell agents to `git reset --hard <branch HEAD>` first and write `sdk.dir=/opt/android-sdk` to local.properties. Worktrees live in .claude/worktrees/ (gitignored). Parallel agents share the scratchpad: give scratch files unique names.
