# Contributing to Tadu

Thanks for taking the time to contribute. This document covers the workflow; the
coding rules live in [AGENTS.md](AGENTS.md) and the technical background lives in
[docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).

## Before you start

- For small fixes (typos, docs, obvious bugs), open a pull request directly.
- For new features or larger refactors, open an issue first so the approach can be
  agreed before you invest time.
- Search existing issues and pull requests before creating a new one.
- Report security issues privately — see [SECURITY.md](SECURITY.md).

## Development setup

Follow [docs/DEVELOPMENT.md](docs/DEVELOPMENT.md). In short:

1. Install JDK 17 and Android SDK 35.
2. Clone the repo and open it in Android Studio.
3. Add `app/google-services.json` (your own Firebase project or the placeholder).
4. Run `./gradlew assembleDebug`.

## Workflow

1. Fork the repository (or create a branch if you have write access).
2. Branch from `master` using a descriptive name:
   - `feat/task-templates`
   - `fix/reminder-timezone`
   - `docs/contributing`
   - `chore/dependency-updates`
3. Make one logical change. Keep refactors separate from behaviour changes.
4. Add or update tests.
5. Run the full local check:

   ```bash
   ./gradlew lintDebug testDebugUnitTest assembleDebug
   ```

6. Update `CHANGELOG.md` under `[Unreleased]` for user-visible changes and update docs
   if setup, commands, or architecture changed.
7. Open a pull request and fill in the template.

## Commit messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add task templates
fix: reschedule reminders after timezone change
docs: document Firestore security rules
test: cover TaskRepository soft delete
build: bump Room to 2.7.1
ci: cache Gradle dependencies
chore: remove unused imports
```

Keep the subject imperative and under ~72 characters. Explain the "why" in the body
when it is not obvious.

## Pull request checklist

- [ ] The change is scoped to one problem.
- [ ] `./gradlew lintDebug testDebugUnitTest assembleDebug` passes.
- [ ] New behaviour has tests, or the PR explains why it cannot be tested.
- [ ] No secrets, keystores, `google-services.json`, APKs, AABs, or IDE noise are
      committed.
- [ ] New user-facing strings are in `strings.xml`.
- [ ] `CHANGELOG.md` and relevant docs are updated.
- [ ] UI changes include before/after screenshots.

## Code style

- Kotlin official style, 4-space indentation, LF line endings, 120-column limit.
- Follow the layer boundaries and Compose conventions in [AGENTS.md](AGENTS.md).
- Prefer immutable state and small, testable functions.
- Do not introduce a new library or architecture pattern without discussing it in an
  issue first.

## Reporting bugs

Use the bug report issue template. Include:

- Steps to reproduce
- Expected vs. actual behaviour
- Device model and Android version
- App version (or commit SHA)
- Relevant logs (redact personal data)

## Community

By participating you agree to follow the [Code of Conduct](CODE_OF_CONDUCT.md).
