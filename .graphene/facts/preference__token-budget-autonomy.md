---
category: preference
subject: token-budget-autonomy
---

2026-11-08 owner: during autonomous runs, check usage regularly; when the WEEKLY token limit reaches 10 % (seven_day.utilization >= 0.10 in rate_limit_event of list_events on this session), finish the work in progress and stop. Read via mcp__claude-code-remote__list_events kinds ["rate_limit_event"]. Interpreted as absolute weekly utilization (conservative).
