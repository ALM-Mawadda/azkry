# Azkry iOS (أذكاري)

Native iPhone port of Azkry: an Arabic-first, offline-first app for adhkar,
prayer times, worship tracking, Quran reading, qibla, Hijri calendar, and local
reminders. It uses SwiftUI, Observation, SwiftData, and structured concurrency.
There is no Supabase, account system, or backend.

The folder and code organization follow the proven conventions of
`~/tawsil-app/tawsil_ios`, adapted to Azkry's local-only data model. The Android
and iOS apps share product behavior and authoritative bundled religious data,
while each platform owns its runtime implementation.

## Requirements

- Xcode 26+
- XcodeGen (`brew install xcodegen`)
- An iOS 26+ simulator or device

## Getting Started

```bash
cd azkary_ios
xcodegen generate
open Azkry.xcodeproj
```

Build from the command line:

```bash
xcodebuild \
  -project Azkry.xcodeproj \
  -scheme Azkry \
  -destination 'platform=iOS Simulator,name=iPhone 17 Pro' \
  build
```

Run tests by replacing `build` with `test`. The app needs no secrets or network
configuration. Device signing uses the developer team selected in Xcode.

## Architecture

```
AzkryApp
  ├─ AppState (app-level navigation/preferences)
  ├─ AppBootstrapper (local database readiness + bundled seed)
  ├─ RootView
  └─ MainShellView
      └─ Feature view
          └─ @MainActor @Observable ViewModel
              └─ Service protocol
                  ├─ SwiftData / UserDefaults
                  ├─ bundled JSON
                  └─ pure local calculation
```

- **UI:** SwiftUI, Arabic locale, forced RTL, Dynamic Type-aware layouts.
- **State:** Observation (`@Observable`) view models isolated to `@MainActor`.
- **Persistence:** SwiftData for structured content and progress; UserDefaults
  only for small preferences.
- **Concurrency:** `async/await` and lifecycle-bound SwiftUI `.task` work.
- **Navigation:** state-driven root shell plus `NavigationStack` for pushed flows.
- **Project manifest:** `project.yml` is authoritative; regenerate the Xcode
  project after manifest changes.

No Repository or UseCase layers are added. The feature flow is intentionally:

```
View -> ViewModel -> Service -> SwiftData / preference / calculation
```

See [AGENTS.md](./AGENTS.md) for the complete ownership and design contract.

## Bundled Data

- `Azkry/Resources/Adhkar/athkar_seed.json` plus `azkry_extra_seed.json`: the
  complete 18-category, 374-item runtime library synchronized with Android.
- `Azkry/Resources/Quran/`: complete Uthmani Quran dataset (114 surahs, 6236
  ayahs), copied byte-for-byte from the Android app's public dataset.
- `Azkry/Resources/Fonts/`: Almarai, Amiri, and Amiri Quran under SIL OFL.
- `Azkry/Resources/PrivacyInfo.xcprivacy`: no tracking or collected-data
  declarations, with the required-reason declaration for local preferences.
- `Azkry/Resources/THIRD_PARTY_NOTICES.txt`: Tanzil and font notices shipped
  inside the app bundle.

Do not hand-edit Quranic text or the imported Arabic adhkar dataset in this
platform folder. Update the canonical source, validate it, and synchronize both
platform bundles. Scholarly review remains mandatory before public release.

## Implemented Features

- XcodeGen project, iPhone-only target, unit/UI test targets
- Arabic RTL app root and dark-first design tokens
- versioned SwiftData schema, recoverable startup, and atomic revision-driven
  adhkar seeding
- full adhkar library with counters, favorites, normalized Arabic search, and
  daily progress
- complete Quran browser/reader with surah, juz, and all 604 page jumps;
  exact last-read position, page bookmarks, and 30/60/90-day khatmah plans
- worship tracking, tasbih counter, live qibla compass with static fallback,
  Friday checklist, the
  `صفحات` reference hub, and Umm al-Qura Hijri calendar
- pure local astronomical prayer-time calculator with target-day DST handling
  and high-latitude/polar fallback
- opt-in local notification for the next obligatory prayer
- Dynamic time-of-day home header with an invariant centered Hijri pill
- appearance settings, city presets and opt-in device location, versioned local
  backup/restore, app icon, privacy manifest, and bundled third-party notices
- unified normalized search across the adhkar library and Quran surah names,
  plus a dedicated route for the four bundled exclusive adhkar collections

## Attribution

- Quran text: Tanzil Uthmani text via the alquran.cloud public dataset.
- Fonts: Almarai, Amiri, Amiri Quran (SIL Open Font License).
- Imported adhkar content: reference Athkar iOS data; scholarly verification is
  required before release.

The MIT license covers original Azkry software. It does not replace the Tanzil
CC BY 3.0 terms, the fonts' SIL OFL, or third-party rights in imported source
texts. See `Azkry/Resources/THIRD_PARTY_NOTICES.txt`.
