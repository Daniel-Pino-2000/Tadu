# Changelog

All notable changes to this project are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/) and
this project intends to follow [Semantic Versioning](https://semver.org/).

## [Unreleased]

### Added

- Contributor guide, code of conduct, security policy, and agent/house rules.
- Architecture and development documentation under `docs/`.
- GitHub issue templates, pull request template, CODEOWNERS, and Dependabot config.
- CI workflow running lint, unit tests, and a debug build.
- Least-privilege Firestore security rules and Firebase CLI config.
- Example `google-services.json` for compile-only builds.
- Exported Room schema and optional release signing configuration.
- `.editorconfig` and `.gitattributes` for consistent formatting.

### Changed

- Consolidated all dependency versions into the Gradle version catalog.
- Migrated Room's annotation processor from kapt to KSP.
- Enabled Gradle parallel builds, build caching, and configuration cache.
- Renamed the Gradle root project from `ToDoList` to `Tadu`.
- Updated the README to reflect Firestore (not Cloud Storage) and current setup steps.
- Replaced the generated unit-test sample with a real `Result` test.

### Removed

- Stopped tracking the real `app/google-services.json`.
- Stopped tracking the built `app/release/app-release.aab` and `.kotlin/` error logs.
- Removed unused dependencies (Accompanist system UI controller, WorkManager, Firebase
  Analytics) and their dead imports.

## [2.0.0]

### Added

- Firebase Authentication: sign up, login, logout, password reset, and account
  deletion with re-authentication.
- Offline-first task storage with Room and per-user Firestore sync.
- Task deadlines, priority levels, labels, location tags, and custom reminder text.
- Reminder scheduling with `AlarmManager`, notification delivery, and rescheduling
  after device reboot.
- Today, Inbox, Search, Calendar, Reminders, History, and Settings screens.
- Add-to-device-calendar support.
- Theme modes, accent palettes, and a global notification toggle.
