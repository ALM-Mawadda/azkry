# Azkry Workspace Agent Notes

This root `AGENTS.md` is the workspace map. Each domain owns its own documentation; do not let one domain's docs become the source of truth for another.

Azkry (أذكاري) is an Arabic-first, offline-first Islamic adhkar app: prayer times, adhkar with tap counters, worship tracking, Quran reading. All data lives in a local on-device database — there is no backend.

## Domains

- `azkary_ios/`
  iPhone app, native SwiftUI runtime, XcodeGen workflow, SwiftData persistence,
  and app behavior
- `azkry_android/`
  Android app, native Kotlin/Compose runtime, Gradle workflow, local Room database, and app behavior

## Start Here

- `azkary_ios/AGENTS.md`
- `azkary_ios/README.md`
- `azkry_android/AGENTS.md`
- `azkry_android/README.md`
- `DESIGN.md`

## Ownership Rules

- Keep iOS implementation, Xcode commands, SwiftUI screens, SwiftData schema,
  and iPhone app configuration in `azkary_ios` docs.
- Keep Android implementation, Gradle commands, Compose screens, database schema, and app configuration in `azkry_android` docs.
- Keep cross-platform product behavior and bundled religious data aligned, but
  let each platform own its implementation details and lifecycle behavior.
- `DESIGN.md` is the durable cross-platform visual contract and the only source
  of truth for the app's look. Each domain's `AGENTS.md` may add platform rules
  but must not make the other platform's documentation its source of truth.
- Use focused inspection and verification rather than broad repo-wide commands when possible.
- Update the relevant domain docs in the same task when runtime behavior, commands, or invariants change.

## Git Rules

- Never run `git add`, `git commit`, or `git push` unless the user explicitly asks. Leave changes unstaged for the user to review.
- This applies to subagents too — when dispatching an agent for edits, instruct it not to stage or commit.
