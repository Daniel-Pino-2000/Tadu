# Security policy

## Supported versions

| Version | Supported |
| --- | --- |
| 2.x | ✅ |
| < 2.0 | ❌ |

Tadu is a personal project maintained on a best-effort basis. Security reports are
taken seriously, but no response-time SLA is offered.

## Reporting a vulnerability

**Do not open a public issue for security problems.**

Preferred: use GitHub's private vulnerability reporting on the
[Security tab](https://github.com/Daniel-Pino-2000/Tadu/security/advisories/new).

If private reporting is unavailable, contact the maintainer
[@Daniel-Pino-2000](https://github.com/Daniel-Pino-2000) directly on GitHub with:

- A description of the issue and its impact
- Steps to reproduce (proof of concept if possible)
- The affected version or commit
- Any suggested fix

You will receive an acknowledgement when the report is seen. Please allow time for a
fix before public disclosure.

## Secrets policy

- `app/google-services.json` is git-ignored and must never be committed. Use
  `app/google-services.json.example` for compile-only builds.
- Keystores, `keystore.properties`, `.env` files, and signing passwords must never be
  committed.
- Firebase Android API keys are client-side identifiers, not server secrets. They
  should still be **restricted** in the Google Cloud console to the app's package name
  and signing certificate SHA-1.
- Earlier commits in this repository may contain a Firebase configuration. If you own
  that Firebase project, restrict or rotate its API key and review the Firestore rules.
- If you find a leaked secret, report it privately and rotate it immediately.

## Firestore rules

The repository includes least-privilege rules in [`firestore.rules`](firestore.rules).
Deploy them with:

```bash
firebase deploy --only firestore:rules
```

Review rule changes carefully: a permissive rule can expose every user's tasks.

## Dependency security

Dependabot is configured for Gradle dependencies and GitHub Actions. Enable Dependabot
alerts and security updates in the repository settings, and review dependency pull
requests before merging.
