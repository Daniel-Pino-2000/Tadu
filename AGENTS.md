# House rules

This file is the shared contract for humans and AI agents working on Tadu. Follow it
for every change. When a rule and the existing code disagree, prefer the rule for new
code and avoid drive-by rewrites of unrelated code.

## Project facts

- Single-module Android app: `:app`
- Namespace / application ID: `com.myapp.tadu`
- Language: Kotlin 2.0, Java 11 bytecode target
- UI: Jetpack Compose (Material 2 and 3), Navigation Compose
- Local data: Room (KSP) + DataStore
- Remote data: Firebase Auth + Cloud Firestore
- minSdk 24, compile/target SDK 35
- Package root: `app/src/main/java/com/myapp/tadu/`

## Commands

```bash
./gradlew assembleDebug        # must pass before opening a PR
./gradlew testDebugUnitTest    # unit tests
./gradlew lintDebug            # Android Lint
./gradlew connectedDebugAndroidTest  # when a device is available
```

CI runs lint, unit tests, and `assembleDebug`. Do not merge with a red build.

## Architecture rules

1. **Respect the layers.** UI → ViewModel → Repository → Room/Firestore.
   Compose code must not call `TaskDao`, `FirebaseAuth`, or `FirebaseFirestore`
   directly.
2. **Room is the read path.** New reads should be Room `Flow`s exposed through the
   repository. Do not add one-off Firestore reads to the UI.
3. **Writes go through the repository.** If a write must also reach Firestore, add it
   to `TaskRepository`, not to the ViewModel.
4. **Keep ViewModels free of Android `Context`.** Use repositories and injected
   abstractions; `AndroidViewModel` is a last resort.
5. **Platform work stays in `notifications/`.** Scheduling goes through
   `ReminderScheduler` so it can be faked in tests.
6. **Dependency wiring lives in `Graph`** (and ViewModel factories). Do not introduce a
   second service locator or a new DI framework without discussing it first.
7. **Navigation uses `Screen` routes**, not ad-hoc string literals scattered in
   composables.

## Kotlin and Compose conventions

- 4-space indentation, LF line endings, UTF-8, max line length 120. See
  `.editorconfig`.
- Kotlin official style; no wildcard imports.
- Prefer `val` and immutable data. Do not add new `var` properties to `Task`; use
  `copy()`.
- Use trailing commas in multi-line parameter/argument lists.
- Name composables with PascalCase nouns (`TaskCard`, not `TaskCardView`); name
  modifiers `modifier: Modifier = Modifier` and pass them down.
- Hoist state out of leaf composables. Keep screen state in ViewModels.
- Use `collectAsStateWithLifecycle()` for flows in UI code.
- Add previews (`@Preview`) for new reusable composables when practical.
- Extract new user-facing strings to `res/values/strings.xml`. Existing hardcoded
  strings may stay until touched.
- Do not add new Material 2 components; new UI uses Material 3. Migrate nearby code
  only when it is part of the task.

## Data and security rules

- Never commit `app/google-services.json`, keystores, `keystore.properties`, `.env`
  files, APKs, or AABs. They are git-ignored for a reason.
- Never log tokens, passwords, email addresses, or full task contents.
- Keep `firestore.rules` least-privilege: users may only read/write their own
  `users/{uid}` document and subcollection.
- New Firestore fields must be mirrored in the Room entity and in exported schemas.
- Do not weaken `android:exported` settings. Receivers should stay `exported="false"`
  unless a system broadcast requires otherwise.
- Prefer `PendingIntent.FLAG_IMMUTABLE` for all new `PendingIntent`s.

## Testing expectations

- Add or update a JVM unit test when changing repository, ViewModel, scheduling, or
  pure utility logic. Use fakes, not the Android framework.
- Add or update a Compose UI test for new user-visible flows when feasible.
- Bug fixes should include a regression test.
- Keep the generated example tests until real tests replace them; do not delete tests
  to make a build pass.

## Git and pull requests

- Branch from `master`; keep branches short-lived (`feat/...`, `fix/...`, `docs/...`).
- Use Conventional Commits: `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `build:`,
  `ci:`, `chore:`.
- One logical change per pull request. Separate refactors from behaviour changes.
- Update `CHANGELOG.md` under `[Unreleased]` for user-visible changes.
- Update docs when commands, architecture, setup, or behaviour changes.

## Definition of done

A change is done when:

- [ ] `./gradlew lintDebug testDebugUnitTest assembleDebug` passes.
- [ ] New behaviour has tests (or a written reason why it cannot be tested).
- [ ] No new secrets, generated binaries, or IDE noise are staged.
- [ ] User-facing strings are in resources (for new code).
- [ ] `CHANGELOG.md` and relevant docs are updated.
- [ ] The PR description explains the change and how it was verified.

## Current improvement priorities

Use these as a starting point, not a mandate to do everything at once:

1. Replace best-effort Firestore writes with a durable outbox + WorkManager.
2. Add `updatedAt` / tombstones and a real merge strategy for sync.
3. Make `Task` immutable and remove `!!` usages.
4. Add repository/ViewModel unit tests with fake DAO and cloud services.
5. Extract hardcoded strings and finish the Material 3 migration.
6. Replace the generated example tests with behaviour tests.
7. Add release signing and a tag-based release workflow.

See `docs/ARCHITECTURE.md` for the full debt list.
