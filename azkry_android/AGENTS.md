# Azkry Android Agent Notes

Use this file as the operator guide for work inside `azkry_android/`. The goal is a native Kotlin/Compose adhkar app with the same discipline as the reference architecture: feature logic stays close to its screen, persistence contracts stay explicit, and shared code is earned. The app is Arabic-first, RTL, dark-first, and fully offline — all data lives in a local Room database.

## Read First

- `README.md`
- `../AGENTS.md`
- `../design/` — the UI contract (screenshots of the target app)

## Package Map

```
app/src/main/kotlin/com/azkry/app/
  AzkryApplication.kt          # Hilt app + notification channels
  MainActivity.kt              # splash, edge-to-edge, reminder-chain re-plan
  app/                         # root composition, app settings, DI bindings
    AzkryRoot.kt               # locale + theme + background + shell
    AppModule.kt               # service interface -> implementation bindings
    AppState.kt                # AppLanguage + DataStore keys
    AppSettingsService.kt / AppSettingsViewModel.kt
  core/
    components/                # genuinely shared Compose UI primitives
                               # (AmbientBackground, SectionRowCard, ProgressRing,
                               #  CircleIconButton, ScreenHeader, CenteredProgress)
    coroutines/                # @ApplicationScope CoroutineScope provider
    database/                  # Room database, DAOs, DI module, first-launch seed
    error/                     # UserFacingMessage marker
    i18n/                      # locale plumbing + StringProvider
    models/                    # Room entities + cross-feature enums (Prayer, labels)
    notifications/             # channels + AlarmManager exact-alarm wrapper
    prayertimes/               # pure-Kotlin astronomical prayer time calculator
    preview/                   # @AzkryPreview, AzkryPreviewSurface, Samples (fixtures only)
    theme/                     # colors, typography, spacing, radius, bundled fonts
                               # (Almarai = app-wide UI face; Amiri/Amiri Quran = religious text)
    utilities/                 # tiny cross-feature helpers (date keys, hijri formatting)
  features/
    main/                      # MainShell: state-driven root navigation
    home/{viewmodels,views}    # hub: verse header, prayer strip, functional tab strip
    adhkar/{services,viewmodels,views}       # categories, dhikr reader, favorites tab
    tracking/{models,services,viewmodels,views}  # worship tracking (prayers/adhkar/tasks)
    mushaf/{models,services,viewmodels,views}    # Quran: bundled text, surah/juz lists, reader
    prayertimes/{services,viewmodels,views}  # calculation service, day tab, editable settings,
                                             # fused location + geocoder
    qibla/{models,services,viewmodels,views} # bearing math + compass sensor + dial tab
    counter/{services,viewmodels,views}      # persistent tasbih counter tab
    friday/{models,services,viewmodels,views} # Friday sunan checklist + fadail page
    pages/{models,services,viewmodels,views}  # صفحات hub: static topic pages (أسماء الله
                                             # الحسنى grid, rawatib, duha, tafsir…) + share
    calendar/{models,viewmodels,views}       # hijri month grid (Umm al-Qura chronology)
    notifications/{models,services,receivers,viewmodels,views}
                               # reminder planning, alarm chain, boot receiver, toggles UI
    settings/{views}           # settings hub
```

Bundled data: `app/src/main/assets/quran/` holds the full Uthmani Quran text
(public Tanzil text via the alquran.cloud dataset) as an `index.json` plus one
JSON file per surah; `res/font/` bundles Almarai, Amiri, and Amiri Quran (all
SIL OFL). `assets/adhkar/athkar_seed.json` is the main adhkar library
(11 categories, 339 items) extracted verbatim from the reference iOS Athkar
app's database — regenerate it with tooling from that source, never edit the
Arabic by hand. Quranic passages in `StaticPagesService` are copied verbatim
from `assets/quran/` — never retype Quranic text by hand; regenerate it so
the tashkeel stays authoritative. Adhkar seeding is revision-driven: bump
`AdhkarSeed.CONTENT_REVISION` when bundled content changes, and know that a
bump wipes and re-inserts all seeded categories (favorites and daily counts
reset via FK cascade).

Design identity (do not regress): Almarai is the app-wide typeface; the home
header is a **time-of-day sky** (`HeaderPhase` from prayer times → gradients,
sun glow, stars only at night via `SkyHeader.kt`); the prayer strip frames
"now" between the passed and upcoming events (`HomeDayView.stripEvents`); the
countdown is yellow with live seconds; scrolling collapses the sky into a
pinned bar (wordmark + actions + tabs via `stickyHeader`); cards are
soft-radius (26dp) with hairline borders; mushaf screens sit on the darker
`MushafBackground`; the hijri chip is two-line (day over month). Header text
is always light — it sits on the sky, not the page.

The folders above are the default target shape. Add feature-local `models/` only when the type is not a database/shared app model.

## Ownership Rules

- `app/` owns application composition: root shell selection, app-level settings, and DI module wiring.
- `core/models/` owns Room entities used by more than one feature or matching database tables directly, plus model-attached domain extensions (e.g. `Prayer.labelRes()`) so wire formats and UI labels stay defined once.
- `core/database/` owns persistence setup and reusable persistence helpers only: the `AzkryDatabase` class, DAOs, the Hilt module, and the first-launch seed. It must not grow feature-specific business logic — feature-shaped queries belong to the DAO, orchestration belongs to feature services.
- `core/prayertimes/` owns the pure calculation (methods, angles, madhab, high-latitude rules). It has no Android dependencies so it stays unit-testable; anything touching DataStore or ZoneId lives in `features/prayertimes/services`.
- `core/components/` owns reusable UI atoms and molecules. Add to it only when **two or more features** consume the component.
- `core/theme/` owns design tokens (`AzkryColors`, `AzkrySpacing`, `AzkryRadius`, `AzkryTextStyles`). Feature packages consume these; they do not define competing color systems. The app is dark-first: `AzkryTheme` always applies the dark scheme until a light design exists.
- `core/preview/` owns `@AzkryPreview` (multi-preview annotation), `AzkryPreviewSurface` (theme-aware preview wrapper), and `Samples` (in-memory fixtures for `@Preview` composables and unit tests). Production code must never reference `Samples`.
- `features/*` owns user-facing behavior. Screen-specific queries, state machines, and UI state belong inside the relevant feature. A feature may keep an internal `models/` folder for screen-only types and a `services/` folder for persistence access — services hold no UI state.
- Adhkar content (categories, texts, repeat counts, sources) lives in the database, seeded from `core/database/seed/AdhkarSeed.kt`. Arabic content strings live in the database; UI chrome strings live in `res/values/strings.xml`.

## Logic Placement

- **Composable views:** render state, call intent handlers, and contain local UI-only state such as a text field draft. No database calls, no long-running coroutines except lifecycle collection helpers.
- **ViewModels:** own `UiState`, orchestration, loading/error flags, and calls into services. Expose a single `StateFlow<UiState>` plus explicit intent functions.
- **Services:** own Room/DataStore/computation side effects for a domain. Services are interfaces with a concrete `Room*`/`DataStore*`/`Calculated*` implementation bound in `AppModule` so tests can substitute fakes.
- **Models:** database-shaped models go in `core/models`; screen-only types stay in the feature package.
- **Navigation:** `features/main/MainShell.kt` owns root routing as plain Compose state (`rememberSaveable`), matching the reference architecture. Do not add a navigation library for new screens without an explicit decision.
- **Utilities:** must be tiny and cross-feature. If only one feature uses it, keep it in that feature.

## Abstraction Rules

- Do not add Repository/UseCase layers. The architecture is ViewModel -> Service -> DAO/DataStore; mirror it.
- Add a new abstraction only when it removes real duplication or has at least two concrete consumers.
- Do not put feature-specific business logic in `core`.
- Do not create "manager" classes that combine UI state, business rules, and persistence. Split by responsibility.
- Prefer small interface seams for tests over broad generic data layers.

## Compose Previews

- **Stateful → stateless split.** Screens with `viewModel = hiltViewModel()` are unpreviewable. Split them in two: a stateful entry (`TrackingView`) that collects state and forwards a snapshot, and a stateless content (`TrackingContent`) that takes only `(state, callbacks)` and is fully previewable.
- **Multi-preview annotation.** Use `@AzkryPreview` from `core/preview/AzkryPreview.kt`. It expands into three panels (Arabic RTL dark — the canonical view — plus small-width and large-font passes).
- **Wrap previews in `AzkryPreviewSurface`** so `AzkryTheme` and the page background apply. Pass `padding = 0.dp` for full-bleed screens.
- **Sample fixtures live in `core/preview/Samples.kt`.** Add representative variants (short/long texts, missing sources, high repeat counts) there; do not invent ad-hoc fixtures inside view files.
- **Place each preview private inside the same file as the composable it covers.** `ui-tooling` is `debugImplementation`, so previews are stripped from release builds.

## State And Concurrency

- ViewModels use immutable `UiState` data classes exposed as `StateFlow` via `stateIn(WhileSubscribed(5_000))`.
- Use `viewModelScope.launch` for user intents and lifecycle-owned work.
- ViewModels that catch `Throwable` MUST rethrow `CancellationException` first; coroutine cancellation is not a recoverable error.
- One-shot UI events (sheet dismiss, snackbar, navigation) flow through `SharedFlow`/`Channel`, never a `Boolean` field on `UiState`.
- Composables that own state representing **navigation** intent (selected screen, open category) must use `rememberSaveable` so the state survives rotation and language switching.
- Keep one source of truth per screen. Avoid mirroring the same mutable state in both composables and ViewModels.

## Data Contracts To Preserve

- Daily records key on the **local ISO date string** (`LocalDate.toDateKey()`, `yyyy-MM-dd`). A new day starts every counter and log at zero implicitly — never add a midnight reset job.
- Dhikr counters are capped at the dhikr's `repeatCount`; increments beyond it are no-ops.
- A category counts as "completed" for tracking when every one of its adhkar reached its repeat count that day.
- Day completion percentage uses `WorshipScoring` weights (prayers 50%, adhkar 10%, Quran 10%, rawatib 10%, daily 10%, other 10%). Change weights only there.
- `Prayer.Sunrise` is not an obligatory prayer; use `Prayer.obligatory` for anything user-facing that counts prayers.
- Schema changes require a Room migration plus a version bump in `AzkryDatabase`; exported schema JSON lives in `app/schemas/`.

## Android-Specific Rules

- The default resource locale is Arabic (`res/values/strings.xml` is Arabic); `MissingTranslation` lint is disabled deliberately. The app forces the Arabic locale and RTL even on non-Arabic devices (`AppSettings.language` defaults to Arabic).
- Directional chevrons/back arrows in shared components are deliberately **not** auto-mirrored: the design's forward chevron points left and its back chevron points right, matching this RTL-only app.
- Activity-level theme parents must extend `Theme.AppCompat.*` (or `Theme.SplashScreen` which already does).
- Backups are disabled (`allowBackup=false` and both extraction rule files exclude databases and prefs) — worship data is private by default. Revisit both files together if a backup feature is ever added.
- Runtime permissions in use: `POST_NOTIFICATIONS` (requested when the first reminder toggle is enabled) and `ACCESS_COARSE_LOCATION` (requested from the prayer-settings location dialog). Exact alarms use `USE_EXACT_ALARM` (33+) / `SCHEDULE_EXACT_ALARM` (31–32) — legitimate for an adhan app.

## Notifications

- All notifications are **local**: a single-outstanding-alarm chain owned by `ReminderScheduler`. `ReminderPlanner` (pure, tested) picks the next enabled occurrence; the alarm receiver shows the notification and re-plans. Boot/timezone/time changes and prayer-settings changes re-plan; `MainActivity` also re-plans on every app open so drifting prayer times stay accurate.
- Never add a periodic "reschedule everything" job; the chain design is the contract.

## Build & Release

- `release` build type runs R8 (`isMinifyEnabled = true` + `isShrinkResources = true`) with `proguard-rules.pro`. Keep rules cover Hilt, Kotlinx Serialization, Room, and coroutines.
- Release signing reads `RELEASE_KEYSTORE_PATH`, `RELEASE_KEYSTORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD` from `secrets.properties` (git-ignored). If unset, the release build falls back to debug signing for local builds; CI must supply them for store uploads.

## Tests

- Tests mirror source packages under `app/src/test/kotlin/com/azkry/app/...`.
- Prioritize ViewModel/service/calculator behavior tests over rendering implementation details.
- Use Turbine for Flow assertions and MockK (or hand-rolled fakes) for service substitutes.
- Add or update focused tests in the same task when changing behavior, state transitions, calculations, or service contracts.

## Commands

- Build debug APK: `./gradlew :app:assembleDebug`
- Run unit tests: `./gradlew :app:testDebugUnitTest`
- Run lint: `./gradlew :app:lintDebug`
- CI parity command: `./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug`

## Configuration

- `local.properties` needs `sdk.dir` pointing at the Android SDK.
- No runtime secrets are required; `secrets.properties` exists only for release signing.

## Documentation Rule

Update `README.md` in the same task if you change Android setup, build commands, runtime configuration, architecture, navigation, database schema, or theme/shared UI.
