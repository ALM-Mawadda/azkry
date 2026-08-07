# Play Store submission sheet

Everything the Play Console asks for, answered from what the code actually does.
Verified against the manifest, the merged manifest, and the built APK — not from
memory. Re-check this file whenever permissions or bundled assets change.

## Build

```bash
./gradlew :app:bundleRelease
```

Output: `app/build/outputs/bundle/release/app-release.aab`. Play requires an
**AAB**, not an APK. Packaging fails loudly if `secrets.properties` is missing —
it never falls back to debug signing. See `secrets.properties.example`.

Enrol in **Play App Signing** at first upload. It lets Google re-sign the app and
means a lost upload key can be reset instead of orphaning the listing.

## Release checklist

- [ ] Keystore created, `secrets.properties` filled, `.jks` backed up off-machine
- [ ] `./gradlew :app:lintDebug :app:testDebugUnitTest :app:bundleRelease` green
- [ ] **Install the release (minified) build and walk every screen.** R8 builds
      cleanly, but Hilt/Room/Glance/serialization failures under minification
      only surface at runtime. This is the single highest-risk unverified step.
- [ ] Privacy policy hosted at a public URL (`PRIVACY.md` is the source text)
- [ ] Data safety form filled per the table below
- [ ] Exact-alarm declaration submitted
- [ ] Content rating questionnaire completed
- [ ] Store assets uploaded

## App details

| Field | Value |
|---|---|
| App name | أذكاري |
| Package | `com.azkry.app` |
| Version | 1.0.0 (versionCode 1) |
| Category | Lifestyle |
| Default language | Arabic (ar) |
| Contains ads | **No** |
| In-app purchases | **No** |
| Target audience | 13+ (general; no age-sensitive content) |

## Data safety

The strong answer here is that the app holds **no `INTERNET` permission** —
verified in the merged manifest. It is incapable of transmitting anything.

| Question | Answer |
|---|---|
| Does your app collect or share any of the required user data types? | **No** |
| Is all user data encrypted in transit? | N/A — no data is transmitted |
| Do you provide a way to delete data? | Yes — uninstalling removes everything |

Location needs care in the wording: the app *accesses* approximate location to
compute prayer times on-device, but does not *collect* it in Play's sense
(collect = transmitted off the device). Declare **not collected, not shared**,
and state the on-device use in the policy — `PRIVACY.md` already does.

## Permissions

| Permission | Why | Console action |
|---|---|---|
| `ACCESS_COARSE_LOCATION` | Prayer times are astronomical and need coordinates. Precise location is never requested. | Covered by the privacy policy |
| `POST_NOTIFICATIONS` | Adhan and adhkar reminders | None |
| `RECEIVE_BOOT_COMPLETED` | Re-plan the alarm chain after reboot | None |
| `USE_EXACT_ALARM` | **Restricted — needs a declaration** | See below |
| `SCHEDULE_EXACT_ALARM` (≤ API 32) | Same, on older releases | Covered by the same declaration |

### Exact-alarm declaration text

> أذكاري is a Muslim prayer-time (adhan) app. Its core, user-facing function is
> calling the five daily prayers at their astronomically calculated times. A
> prayer call delivered even a few minutes late is religiously incorrect —
> Maghrib in particular marks the end of the fasting day. Inexact alarms are
> batched by the system and can drift by many minutes, which would make the
> app's primary purpose unreliable. The app schedules a single outstanding
> alarm for the next prayer and re-plans after it fires; it does not maintain a
> queue of alarms or use exact alarms for any secondary feature.

This falls squarely inside Google's alarm/clock/calendar exemption, but the
declaration is mandatory — omitting it is an automatic rejection.

## Store assets required

| Asset | Spec |
|---|---|
| App icon | 512 × 512 PNG, 32-bit |
| Feature graphic | 1024 × 500 PNG/JPEG |
| Phone screenshots | 2–8, min 320 px, 16:9 or 9:16 |
| Short description | ≤ 80 chars |
| Full description | ≤ 4000 chars |

### Short description (Arabic)

> أذكار وأدعية ومواقيت الصلاة — يعمل بالكامل بدون إنترنت.

### Full description (Arabic)

> تطبيق أذكاري يجمع الأذكار والأدعية ومواقيت الصلاة في مكان واحد، ويعمل بالكامل
> دون اتصال بالإنترنت.
>
> • أذكار الصباح والمساء والنوم والاستيقاظ وأذكار الصلاة وما بعدها
> • أدعية من القرآن الكريم ومن دعاء النبي ﷺ
> • الرقية بالقرآن والسنة
> • مواقيت الصلاة محسوبة فلكياً حسب موقعك، مع العد التنازلي للأذان القادم
> • أوقات النهي عن الصلاة
> • المصحف كاملاً مع الفهرس والعلامات المرجعية والختمة
> • متابعة العبادات، السنن الرواتب، وسنن الجمعة
> • عدّاد التسبيح، بوصلة القبلة، والتقويم الهجري
> • تنبيهات الأذان والأذكار
> • أسماء الله الحسنى وصفحات موضوعية
>
> لا يجمع التطبيق أي بيانات ولا يرسل شيئاً — كل شيء يبقى على جهازك.

## Testing track

New personal developer accounts must run a **closed test with 12+ testers for 14
continuous days** before production access is granted. Start this early — it
dominates the timeline, not the code.
