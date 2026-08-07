# Azkry Android (أذكاري)

Native Android app for Azkry — an Arabic-first, offline-first Islamic adhkar app: prayer times, adhkar with tap counters, worship tracking, and Quran reading. Built with Kotlin, Jetpack Compose, Hilt, and a local Room database. There is no backend: every feature works fully offline on the phone's local database.

The visual contract — dark starry UI, RTL Arabic — is captured in writing in the "Design identity" section of [AGENTS.md](./AGENTS.md).

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

No secrets are needed for debug builds. `secrets.properties` (git-ignored) is read for release signing, and release packaging fails if any value is missing:

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
  - `dhikr_categories`, `adhkar` — content, seeded from `assets/adhkar/athkar_seed.json` (11 categories, 339 items) plus Kotlin-defined extras; revision-driven, so bumping `AdhkarSeed.CONTENT_REVISION` replaces all seeded content on next open.
  - `dhikr_daily_counts` — per-day tap counters, keyed by local ISO date, capped at each dhikr's repeat count.
  - `prayer_logs`, `worship_logs` — per-day worship tracking checkmarks.
- Adhkar rows carry stable seed keys so JSON backup v2 can restore counters and favorites across database reseeds; imports validate the complete document and write all tables in one transaction.
- **DataStore** — app settings and prayer settings (city, coordinates, IANA timezone, calculation method, Asr madhab, high-latitude rule).

Daily data keys on the local ISO date, so a new day naturally starts fresh without reset jobs. Automatic OS backup/device transfer is disabled; users can explicitly export or import the versioned JSON backup through Android's document picker.

## Prayer Times

`core/prayertimes/PrayerTimeCalculator.kt` computes times astronomically (no network, no third-party library): MWL, Egyptian, Umm al-Qura, Karachi, ISNA, and the France 15°/12° angle conventions; Shafii/Hanafi Asr; angle-based, middle-of-the-night, and seventh-of-the-night high-latitude rules. Each configured location stores an IANA timezone, used consistently by displayed times, next-prayer calculations, widgets, and alarms. Accuracy is within a minute or two of established prayer-time implementations, which is the tolerance the settings page communicates to users.

The default location is Mecca (`Asia/Riyadh`). Settings support current-location refresh or manual city/coordinates/timezone entry.

The الصلاة tab in the home strip is a destination, not an inline tab: it opens the full-screen day board (`features/prayertimes/views/PrayerDayScreen.kt`), which shows the calculated adhan times for the configured location, a live countdown to the next prayer, and the current forbidden-prayer window. It shows **adhan times only** — iqama times are set by each mosque and cannot be calculated, so the app does not display them. The one derived time it does show is the start of Ishraq, computed from the calculated sunrise.

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
| Home tabs: القبلة (compass), المفضلة, العداد | Working |
| الصلاة day board (full screen: next-prayer countdown, forbidden times, day stepping) | Working |
| Adhkar categories + dhikr reader (counters, favorites, details sheet, auto-advance, celebration) — 11 categories / 339 items with titles, virtues, sources | Working |
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

Shown in-app at settings → التراخيص والمصادر, which is a licence obligation
rather than an about page: the adhan recording and the fonts both require their
notices to reach the user. The list lives in
`features/settings/models/AppLicense.kt`; the full OFL text ships at
`assets/licenses/OFL-1.1.txt`.

- Quran text: Tanzil Uthmani text via the alquran.cloud public dataset,
  Creative Commons Attribution 3.0, used unmodified.
- Fonts: Almarai, Amiri, Amiri Quran (SIL Open Font License 1.1).
- Adhan recording: "The Adhan — Muslim Call to Prayer" by Aaqib Azeez,
  Wikimedia Commons, CC BY-SA 4.0, used unmodified.

## Roadmap / TODO

- Per-prayer adhan sound selection; full-screen منبه الصلاة alarm mode.
- Multi-language (the settings row is display-only), Quran translations.
- Widget theming options and a prayer-times week widget.
