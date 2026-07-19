# Azkry iOS Agent Notes

Use this file as the operator guide for work inside `azkary_ios/`. The app is
the native iPhone port of Azkry: Arabic-first, RTL, offline-first, and backed
only by local on-device persistence. There is no Supabase or other backend.

This domain intentionally follows the architecture and operating discipline
of `~/tawsil-app/tawsil_ios`: XcodeGen is the project source of truth, SwiftUI
views render state and user intent, `@MainActor @Observable` view models own
screen state, protocol-backed services own side effects, and tests substitute
focused fakes at those protocol seams.

## Read First

- `README.md`
- `project.yml`
- `../AGENTS.md`
- `../DESIGN.md`
- `Azkry/App/AzkryApp.swift`
- `Azkry/App/AppBootstrapper.swift`
- `Azkry/App/RootView.swift`
- `Azkry/Features/Main/MainShellView.swift`
- `Azkry/Features/Home/ViewModels/HomeViewModel.swift`
- `Azkry/Features/Home/Views/HomeView.swift`
- `Azkry/Features/Adhkar/Services/SwiftDataAdhkarLibraryService.swift`
- `Azkry/Features/PrayerTimes/Services/CalculatedPrayerScheduleService.swift`
- The "Design identity" section below

## Structure

```
Azkry/
  App/                         # entry point, app state, bootstrap, root routing
  Core/
    Components/                # shared SwiftUI atoms used by 2+ features
    I18n/                      # Arabic locale and formatting helpers
    Location/                  # Core Location wrapper for prayer configuration
    Models/                    # SwiftData entities and cross-feature enums
    Navigation/                # root destinations and tab values
    Notifications/             # local UNUserNotificationCenter plumbing
    Persistence/               # SwiftData ModelContainer and schema setup
    PrayerTimes/               # pure astronomical calculator
    Preview/                   # preview fixtures; never imported by production logic
    Theme/                     # colors, typography, spacing, radii, bundled fonts
    Utilities/                 # focused cross-feature helpers
  Extensions/                  # small platform/type extensions
  Resources/
    Adhkar/                    # bundled 18-category / 374-item library
    Quran/                     # bundled full Uthmani Quran dataset
    Fonts/                     # Almarai, Amiri, Amiri Quran (SIL OFL)
  Features/
    Main/                      # state-driven root shell
    Home/{Models,Services,ViewModels,Views}
    Adhkar/{Models,Services,ViewModels,Views}
    Tracking/{Models,Services,ViewModels,Views}
    Mushaf/{Models,Services,ViewModels,Views}
    PrayerTimes/{Models,Services,ViewModels,Views}
    Qibla/{Models,Services,ViewModels,Views}
    Counter/{Models,Services,ViewModels,Views}
    Friday/{Models,Services,ViewModels,Views}
    Pages/{Models,Services,ViewModels,Views}
    Calendar/{Models,Services,ViewModels,Views}
    Notifications/{Models,Services,ViewModels,Views}
    Settings/{Models,Services,ViewModels,Views}
    Search/{Models,Services,ViewModels,Views}
AzkryTests/                    # mirrors production feature paths
AzkryUITests/                  # page objects and user scenarios
```

Feature folders add `Services/Protocols/` and `Views/Components/` only when
needed. Keep feature-specific models within the feature; move a type to
`Core/Models` only when it is a persisted entity or has multiple real owners.

## Tooling And Commands

- Prefer focused Xcode inspection/build/test commands and `rg`/`rg --files`.
- `project.yml` is the source of truth for targets, build settings, resources,
  and future Swift packages. Run `xcodegen generate` after changing it.
- Build the simulator app:
  `xcodebuild -project Azkry.xcodeproj -scheme Azkry -destination 'platform=iOS Simulator,name=iPhone 17 Pro' build`
- Run unit tests:
  `xcodebuild -project Azkry.xcodeproj -scheme Azkry -destination 'platform=iOS Simulator,name=iPhone 17 Pro' test`
- The app has no runtime secrets or backend configuration.

## Coding Patterns To Preserve

- Keep logic close to the feature. Shared code is earned by a second consumer.
- SwiftUI views render immutable state and emit user intent. They do not query
  SwiftData, calculate prayer times, schedule notifications, or own long-lived
  unstructured tasks.
- View models are `@MainActor @Observable` and own screen state, orchestration,
  loading/error state, and lifecycle-bound `async` flows.
- Services own SwiftData, UserDefaults, file, location, notification, and pure
  calculation side effects. Put a small protocol in `Services/Protocols` when
  tests need to substitute the behavior or when multiple implementations exist.
- Prefer value types for domain models unless SwiftData or platform delegate
  semantics require a class.
- Keep one source of truth per screen. Do not mirror the same mutable state in
  both a view and its view model.
- Use Swift structured concurrency. Tie loops to a SwiftUI `.task`; check
  cancellation and do not hide `CancellationError` as a user-facing failure.
- Use `NavigationStack` for pushed flows and typed destination values. Root tab
  selection belongs to `MainShellView`, not to feature view models.
- Use semantic accessibility labels and preserve Dynamic Type. Do not encode
  meaning only through color.
- Appearance mode lives in `@AppStorage(StorageKeys.appearanceMode)` and is
  applied once at `AzkryApp`; individual views never force a color scheme.

## Persistence And Data

- SwiftData is the only database. `AzkrySchema` owns the `ModelContainer`; feature
  services receive a `ModelContext` and keep fetch/mutation logic out of views.
- The bundled `Resources/Adhkar/athkar_seed.json` and
  `azkry_extra_seed.json` form the 18-category, 374-item runtime library. They
  are synchronized with Android. Do not hand-edit imported Arabic text. Change
  it at the canonical source, verify it, then synchronize both platform copies.
- Adhkar seeding is revision-driven. A revision change replaces seeded content;
  stable keys use `category/item_N` (or the explicit item key) and must never be
  repurposed after release. Validate the complete replacement before entering
  one SwiftData transaction. Retain progress for surviving stable keys, cap
  counts to changed repeat limits, and remove progress only for deleted keys.
- The full Quran lives under `Resources/Quran`. Never retype Quranic text by
  hand. Regenerate or copy from the authoritative bundled dataset so Uthmani
  text and tashkeel remain byte-consistent across platforms.
- Daily records use a local ISO `yyyy-MM-dd` date key. A new day starts naturally
  with no midnight reset job.
- Dhikr counts are capped at each item's repeat count.
- UserDefaults is for small preferences only. Structured user data belongs in
  SwiftData; explicit backup uses a versioned validated JSON document. Validate
  the full import before one transaction so a malformed file cannot partially
  replace user progress.
- Quran reading state uses stable value models in UserDefaults: exact last-read
  ayah/page, page-keyed bookmarks, and one optional khatmah plan. All three are
  part of validated backup/restore. Page navigation is derived from the bundled
  Quran assets; never maintain a second hand-authored page index.

## Design Identity

- Arabic and right-to-left layout are canonical. The app injects Arabic locale
  and `.rightToLeft` at the root, independent of the simulator language.
- Almarai is the app-wide UI face. Amiri is for dua/adhkar body text and Amiri
  Quran is for mushaf text.
- The home header is a time-of-day sky derived from prayer times: dawn glow at
  Fajr, blue daylight, warm afternoon, dusk at Maghrib, and stars only at night.
- The prayer strip frames now between the previous and upcoming events. Its
  Hijri day/month pill is absolutely centered; side prayer labels divide the
  remaining width and must adapt without ever moving the center pill.
- The Hijri pill and prayer strip are one visual surface: identical fill, a soft
  outline, the pill rising above the strip and its bottom tucked/clipped into the
  strip. Do not draw it as a separate floating card.
- Scrolling progressively fades the expanded sky content before the compact
  wordmark/actions/tab bar pins at the top.
- The countdown uses a yellow reading-start accent, a human Arabic phrase, and
  a precise muted ticker opposite it.
- Header text stays light because it sits on the sky. Cards use 26-point soft
  corners and hairline borders; mushaf screens use the darker paper surface.
- Do not replace the written visual contract with generic gradients, arbitrary
  icon tiles, or dashboard-style cards. Native iOS behavior may differ where
  accessibility or platform conventions require it, but the visual hierarchy
  and geometry remain the same.

## Testing Defaults

- Update focused tests in the same task as behavior changes.
- Prefer Swift Testing (`import Testing`, `@Suite`, `@Test`, `#expect`) for unit
  tests and XCUITest page objects for end-to-end flows.
- Prioritize pure prayer calculations, HomeDayView phase/strip logic, seeding,
  count caps, date rollover, notification planning, backup validation, and view
  model state transitions.
- Production code never references preview or test fixtures.

## High-Level Invariants

- Everything works offline after install; no feature may silently depend on a
  remote API.
- Prayer calculations use the configured coordinates and IANA time zone from
  calculation through display and notification scheduling.
- Notifications are local. Schedule only the next relevant occurrence and
  re-plan after settings changes and app activation; do not maintain an
  unbounded queue of stale prayer alarms.
- Religious content requires scholarly review before public release even when
  it was imported from the reference app.
- Every bundled adhkar category must remain reachable. The `x_` collections are
  intentionally separated behind the `حصريات` route, not omitted from the UI.

## Documentation Rule

Update `README.md` and this file in the same task when changing iOS setup,
project generation, architecture, persistence, navigation, notification
behavior, bundled-data ownership, theme, or shared UI invariants. Backend and
Android implementation details remain in their owning domains.

## Git Rule

Never run `git add`, `git commit`, or `git push` unless the user explicitly asks.
Leave changes unstaged for review. This applies to delegated work as well.
