# Development guide

## Toolchain

| Tool | Version |
| --- | --- |
| JDK | 17 (required by AGP 8.10; JDK 21 also works) |
| Gradle | 8.11.1 (via wrapper) |
| Android Gradle Plugin | 8.10.0 |
| Kotlin | 2.0.21 |
| compileSdk / targetSdk | 35 |
| minSdk | 24 |
| Android Studio | Meerkat Feature Drop (2024.3.2) or newer recommended |

Use the Gradle wrapper (`./gradlew`) so everyone builds with the same Gradle version.
Do not commit `local.properties`; Android Studio generates it with your SDK path. On a
machine without Android Studio, create it manually:

```properties
sdk.dir=/path/to/Android/Sdk
```

## Local setup

1. Clone the repository and open it in Android Studio.
2. Add your Firebase configuration at `app/google-services.json` (see the README).
   `app/google-services.json.example` is a compile-only placeholder.
3. Let Gradle sync, then run the `app` configuration.

## Common commands

```bash
./gradlew assembleDebug               # build debug APK
./gradlew installDebug                # install on a connected device
./gradlew testDebugUnitTest           # JVM unit tests
./gradlew lintDebug                   # Android Lint
./gradlew connectedDebugAndroidTest   # instrumentation tests (device required)
./gradlew assembleRelease             # minified release build
./gradlew clean                       # remove build outputs
```

Run `./gradlew tasks` to list everything available.

## Continuous integration

`.github/workflows/ci.yml` runs on every push to `master` and on every pull request:

1. Validates the Gradle wrapper checksum.
2. Creates `app/google-services.json` from the placeholder example (CI has no real
   Firebase project).
3. Runs `lintDebug`, `testDebugUnitTest`, and `assembleDebug`.
4. Uploads lint/test reports when a step fails.

Keep the build green. A pull request should not be merged with failing checks.

## Release process

1. Bump `versionCode` and `versionName` in `app/build.gradle.kts`.
2. Update `CHANGELOG.md`.
3. Commit, tag the release (`git tag vX.Y.Z`), and push the tag.
4. Build the release bundle: `./gradlew bundleRelease` (output under
   `app/build/outputs/bundle/release/`).

### Release signing

Release builds are unsigned unless a `keystore.properties` file exists at the repo
root. That file is git-ignored; never commit it or the keystore itself.

```properties
# keystore.properties (local only)
storeFile=keystore/tadu-release.jks
storePassword=********
keyAlias=tadu
keyPassword=********
```

Create the keystore once with:

```bash
mkdir -p keystore
keytool -genkeypair -v -keystore keystore/tadu-release.jks \
  -alias tadu -keyalg RSA -keysize 2048 -validity 10000
```

Back it up securely. Losing the keystore means you cannot publish updates to an
existing Play Store listing.

## Versioning and branches

- `master` is the integration branch and should stay releasable.
- Use short-lived branches: `feat/task-templates`, `fix/reminder-dst`, `docs/...`,
  `chore/...`.
- Commit messages follow [Conventional Commits](https://www.conventionalcommits.org/):
  `feat:`, `fix:`, `refactor:`, `docs:`, `test:`, `build:`, `ci:`, `chore:`.
- Squash merge pull requests and delete the branch afterwards.

## Database changes

Room schemas are exported to `app/schemas/` and committed. When you change an entity:

1. Bump the database version in `TaskDatabase`.
2. Add a `Migration` and register it in `Graph` (or use destructive migration only for
   pre-release builds).
3. Build once to regenerate the schema JSON, then commit both the code and the schema.
4. Add a migration test.

## GitHub repository settings

Recommended settings for the hosted repository (configure in **Settings**):

- Enable **Issues** and **Discussions**; disable the wiki unless you plan to use it.
- Enable **Dependabot alerts** and **Dependabot security updates**.
- Protect `master`: require the CI check to pass, require a pull request for changes,
  and disallow force pushes. For a solo maintainer, start with "require status checks"
  and add review requirements when collaborators join.
- Enable **Automatically delete head branches**.
- Add a repository description and topics (Android, Kotlin, Jetpack Compose, Room,
  Firebase, offline-first).

## Troubleshooting

| Symptom | Fix |
| --- | --- |
| `File google-services.json is missing` | Copy `app/google-services.json.example` to `app/google-services.json` or add your real Firebase config. |
| `SDK location not found` | Create `local.properties` with `sdk.dir=...`, or set `ANDROID_HOME`. |
| Reminders do not fire on Android 12+ | Grant "Alarms & reminders" special access; the app falls back to inexact alarms otherwise. |
| No notifications on Android 13+ | Grant `POST_NOTIFICATIONS`; also check the in-app notification toggle. |
| Reminders stop after reboot | Ensure the app is not restricted by aggressive battery optimisation and that `BOOT_COMPLETED` is allowed. |
| Gradle configuration-cache error | Re-run with `--no-configuration-cache` and report the task; configuration cache is enabled in `gradle.properties`. |
| Room schema error after an entity change | Bump the database version and add a migration; do not edit exported schema JSON by hand. |
| Lint fails in CI but not locally | Run `./gradlew lintDebug` with the same JDK and check `app/build/reports/lint-results-debug.html`. |
