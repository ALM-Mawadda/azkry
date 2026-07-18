# Azkry Workspace Agent Notes

This root `AGENTS.md` is the workspace map. Each domain owns its own documentation; do not let one domain's docs become the source of truth for another.

Azkry (أذكاري) is an Arabic-first, offline-first Islamic adhkar app: prayer times, adhkar with tap counters, worship tracking, Quran reading. All data lives in a local on-device database — there is no backend.

## Domains

- `azkry_android/`
  Android app, native Kotlin/Compose runtime, Gradle workflow, local Room database, and app behavior
- `design/`
  Reference screenshots of the target UI. The app is Arabic, RTL, dark-themed; screens must match these visuals. This folder is documentation, not runtime source.

## Start Here

- `azkry_android/AGENTS.md`
- `azkry_android/README.md`

## Ownership Rules

- Keep Android implementation, Gradle commands, Compose screens, database schema, and app configuration in `azkry_android` docs.
- The design screenshots in `design/` are the visual contract. When a screen diverges from its screenshot, the screenshot wins unless a platform rule forces a deviation.
- Use focused inspection and verification rather than broad repo-wide commands when possible.
- Update the relevant domain docs in the same task when runtime behavior, commands, or invariants change.

## Git Rules

- Never run `git add`, `git commit`, or `git push` unless the user explicitly asks. Leave changes unstaged for the user to review.
- This applies to subagents too — when dispatching an agent for edits, instruct it not to stage or commit.
