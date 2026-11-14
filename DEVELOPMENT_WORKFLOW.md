# Akıllı Tarif — Kontrollü Geliştirme İş Akışı

## Product and design authority
The user is the product owner and final decision-maker. The user describes the requested work. The assistant may offer ideas and explain trade-offs, but implementation starts only after the user approves the scope.

Preserve the current SmartPantryChef design. Do not introduce a new design system or substantially change existing screens based on personal preference. Use the current app and the user's screenshots as references. When a visual change is proposed, show or describe the proposal and wait for approval.

## Roles (review responsibilities)
These are review responsibilities, not a claim that separate autonomous agents are always running:
- **Coordinator:** clarify the request, inspect the project, define scope, and report progress.
- **Android implementation:** follow existing Kotlin and Jetpack Compose patterns.
- **UI/UX review:** check consistency with the established app design and accessibility.
- **Data/migration review:** inspect Room entities, DAOs, migrations, and persistence impact.
- **Test/review:** inspect diffs, add focused tests, run checks, and report risks.

A role can be performed by the assistant or an available coding tool. Never imply an independent agent performed work unless that tool was actually used.

## Required workflow for each task
1. **Understand:** restate the requested outcome and acceptance criteria.
2. **Inspect:** check the latest `main`, relevant files, existing patterns, and tests. Do not assume older notes are still accurate.
3. **Plan:** list the files likely to change, implementation approach, and verification steps. Ask for approval if scope or design is unclear.
4. **Branch:** create a task-specific branch from the latest `main`. Do not work directly on `main`.
5. **Implement:** make only approved, task-related changes. Avoid unrelated cleanup and dependencies.
6. **Review:** inspect the complete diff for regressions, design drift, data loss, security issues, and duplicated logic.
7. **Test:** run focused tests and the repository's baseline CI command, `./gradlew testDebugUnitTest`, when available.
8. **PR:** open a pull request to `main` with a clear summary, test results, and known limitations. Keep it unmerged.
9. **User verification:** share the PR link and wait for the user's explicit approval. Only after approval may the change be merged. The user then runs `git pull --ff-only origin main` in Android Studio and tests with Run.
10. **Close the loop:** record the result and any follow-up task.

## Safety gates
- No direct commits to `main`.
- No merge, force-push, destructive database migration, or repository setting change without explicit user approval.
- No unrelated UI redesign or feature expansion.
- No secrets in source control.
- If the branch has unexpected changes or conflicts, stop and ask rather than overwriting work.
- Never report unrun tests as passing.

## User-facing completion report
Every task report must include:
- What was implemented.
- Files changed.
- Tests/checks actually run and their actual results.
- PR link and merge status.
- What the user should do next in Android Studio.
