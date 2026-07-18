# Azkry Android (أذكاري)

Native Android app for Azkry — an Arabic-first, offline-first Islamic adhkar app: prayer times, adhkar with tap counters, worship tracking, and (later) Quran reading. Built with Kotlin, Jetpack Compose, Hilt, and a local Room database. There is no backend: every feature works fully offline on the phone's local database.

The visual contract is the set of screenshots in [`../design/`](../design) — dark starry UI, RTL Arabic.

## Requirements

- JDK 17
- Android SDK (compileSdk 36); set `sdk.dir` in `local.properties`
- Android Studio (latest stable) or plain Gradle

## Getting Started

```bash
cd azkry_android
./gradlew :app:assembleDebug          # build debug APK
./gradlew :app:testDebugUnitTest      # unit tests
./gradlew :app:lintDebug              # lint
./gradlew :app:lintDebug :app:testDebugUnitTest :app:assembleDebug   # CI parity
```

No secrets are needed to build. `secrets.properties` (git-ignored) is only read for release signing:

```
RELEASE_KEYSTORE_PATH=...
RELEASE_KEYSTORE_PASSWORD=...
RELEASE_KEY_ALIAS=...
RELEASE_KEY_PASSWORD=...
```

## Architecture

Single-module app (`:app`) with a `core` / `features` split and no Repository/UseCase layers:

```
ViewModel  ->  Service (interface)  ->  Room DAO / DataStore / pure calculation
```

- **`app/`** — root composition (`AzkryRoot`), app settings, and the Hilt `AppModule` that binds every service interface to its implementation.
- **`core/`** — shared plumbing: theme tokens, shared components, Room database + seed, the pure-Kotlin prayer time calculator, i18n, preview infrastructure.
- **`features/`** — one package per screen area (`home`, `adhkar`, `tracking`, `prayertimes`, `settings`, `main`), each with `services` / `viewmodels` / `views`.

Navigation is plain Compose state in `features/main/MainShell.kt` (no navigation library): an enum for the current screen plus a saved category id for the dhikr reader overlay.

See [`AGENTS.md`](./AGENTS.md) for the full package map, ownership rules, and coding conventions.

## Data

All persistence is local:

- **Room** (`core/database/AzkryDatabase.kt`, schema exported to `app/schemas/`):
  - `dhikr_categories`, `adhkar` — content, seeded from `assets/adhkar/athkar_seed.json` (the full reference iOS Athkar library: 11 categories, 339 items) plus Kotlin-defined extras; revision-driven, so bumping `AdhkarSeed.CONTENT_REVISION` replaces all seeded content on next open.
  - `dhikr_daily_counts` — per-day tap counters, keyed by local ISO date, capped at each dhikr's repeat count.
  - `prayer_logs`, `worship_logs` — per-day worship tracking checkmarks.
- **DataStore** — app settings (language) and prayer settings (city, coordinates, calculation method, Asr madhab, high-latitude rule).

Daily data keys on the local ISO date, so a new day naturally starts fresh without reset jobs. Backups are disabled: worship data never leaves the device.

## Prayer Times

`core/prayertimes/PrayerTimeCalculator.kt` computes times astronomically (no network, no third-party library): MWL, Egyptian, Umm al-Qura, Karachi, ISNA, and the France 15°/12° angle conventions; Shafii/Hanafi Asr; angle-based, middle-of-the-night, and seventh-of-the-night high-latitude rules. Accuracy is within a minute or two of reference implementations, matching the tolerance the design's settings page communicates to users.

The default location is Mecca until the user configures their city in settings (auto-location is a later feature).

## Quran (Mushaf)

The full Uthmani text ships with the app (public Tanzil text via the
alquran.cloud dataset, `assets/quran/`): an index of the 114 surahs and 30
juz plus one JSON file per surah, parsed on demand so no full-Quran blob sits
in memory. The reader groups ayahs by Madani mushaf page, renders with the
bundled Amiri Quran font (SIL OFL), and persists the last-read position.

## Notifications

Adhan and morning/evening adhkar reminders are fully local — no backend, no
FCM. A single-outstanding-alarm chain (`ReminderScheduler` +
`ReminderPlanner`) schedules the next enabled occurrence with an exact alarm;
firing shows the notification and plans the next. Reboots, timezone changes,
prayer-settings changes, and app opens all re-plan the chain. Toggles live in
settings → إشعارات الأذكار والأذان.

## Screens (current state)

| Screen | Status |
| --- | --- |
| Home hub (verse header, prayer strip, adhan countdown, sections) | Working |
| Home tabs: الصلاة (day times), القبلة (compass), المفضلة, العداد | Working |
| Adhkar categories + dhikr reader (counters, favorites, details sheet, auto-advance, celebration) — full iOS Athkar library: 11 categories / 339 items with titles, virtues, sources | Working |
| Search (normalized Arabic across adhkar + surah names) | Working |
| Mushaf (surah/juz/pages tabs, reader, bookmarks, khatmah, last-read) | Working |
| Worship tracking (week rings, prayers, adhkar, Quran, rawatib) | Working |
| Friday page (sunan checklist, salawat, fadail, Kahf shortcut) | Working |
| صفحات hub (asma ul-husna grid, rawatib, duha, dua etiquette, forbidden times, Ramadan qada, funeral duas, ayah + tafsir, سيد الاستغفار share card) | Working |
| Hijri calendar (Umm al-Qura grid + ±2 day offset setting) | Working |
| Prayer settings (method/madhab/high-lat pickers, manual + auto location) | Working |
| Notifications (adhan sound, pre-adhan, adhkar reminders, ongoing next-prayer, deep links) | Working |
| Exclusive section (العمرة، الحج، أذكار الأحبة، أذكار للصغار، التاريخ) | Working |
| Home-screen widgets (next prayer, daily dhikr) | Working |
| Light / dark / system appearance | Working |
| Backup export/import (JSON via SAF) | Working |
| Adhan per-prayer sound selection, audio playback (streams) | Not started |

## Attribution

- Quran text: Tanzil Uthmani text via the alquran.cloud public dataset.
- Fonts: Almarai, Amiri, Amiri Quran (SIL Open Font License).
- Adhan recording: "The Adhan — Muslim Call to Prayer" by Aaqib Azeez,
  Wikimedia Commons, CC BY-SA 4.0.

## Roadmap / TODO

- Scholarly review of all adhkar texts (mandatory before public release) and a full tashkeel pass over the hadith-based items.
- Per-prayer adhan sound selection; full-screen منبه الصلاة alarm mode.
- Multi-language (the settings row is display-only), Quran translations.
- Widget theming options and a prayer-times week widget.
