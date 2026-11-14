# Akıllı Tarif — Agent Instructions

## Project principles
- This is an existing Android application. Extend the current implementation; do not redesign or rebuild it from scratch.
- Preserve the established SmartPantryChef visual identity: warm cream backgrounds, orange accents, rounded recipe cards, familiar typography, spacing, icon style, and the existing bottom navigation. Treat the current app UI and user-provided screenshots as the design source of truth.
- The product owner (the user) decides what to build. You may suggest improvements, but do not implement a suggestion until the user explicitly approves it.
- Keep changes focused on the requested task. Do not add unrelated features, screens, dependencies, or architecture changes.
- Never claim that a feature, test, build, or deployment succeeded unless its result was actually observed.

## Before changing code
1. Inspect the current branch, recent commits, relevant source files, navigation, data models, persistence, and tests.
2. Summarize the proposed change, affected files, risks, and test plan.
3. If the task is ambiguous or would change product behavior/design materially, ask the user before coding.
4. Check for existing components and patterns before creating new ones. Reuse them where appropriate.

## Architecture and implementation
- Follow the existing Kotlin, Jetpack Compose, Room, and Gradle conventions already used in the repository.
- Do not assume a backend is operational merely because a dependency or configuration exists. Verify the current implementation before relying on it.
- Preserve existing Room data. Do not introduce destructive migrations or remove migration paths. Review and test every schema change.
- Do not hardcode secrets, API keys, credentials, or private user data.
- Keep UI state, navigation, repository, and persistence responsibilities consistent with nearby code.
- Add or update focused unit tests for new logic and bug fixes.
- Keep accessibility in mind: meaningful content descriptions, usable touch targets, and readable contrast.

## Git and approval rules
- Never push directly to `main`, force-push, merge a pull request, or change repository settings.
- Work on a task-specific branch created from the latest `main`.
- Keep commits small and use descriptive messages.
- Open a pull request to `main` for review. Do not merge it.
- The user must review and explicitly approve the changes before they are merged.
- Do not overwrite unrelated user changes. If the working tree or branch contains unexpected changes, stop and report them.

## Verification and reporting
- Run the relevant tests and checks available in the repository. The existing CI workflow currently runs `./gradlew testDebugUnitTest`; use that as the baseline test command.
- Report exactly what changed, which files changed, commands run, test results, and any remaining risks.
- If a test or build could not be run, say so plainly.
- After a PR is ready, tell the user the PR link and wait for approval. The user will pull the approved `main` branch into Android Studio and verify the app with Run.
