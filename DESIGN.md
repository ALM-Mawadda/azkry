# Azkry Product Design Contract

This file is the cross-platform written substitute for the private reference
screenshots. Platform-specific implementation details remain in each domain's
`AGENTS.md`; this document owns only the shared visual hierarchy.

- Arabic and right-to-left layout are canonical.
- Almarai is the interface face, Amiri is used for adhkar and dua, and Amiri
  Quran is used for Quran text.
- The expanded home header is a prayer-aware sky: dawn at Fajr, clear daylight,
  warm afternoon, dusk at Maghrib, and stars only at night.
- Its prayer strip shows the event before now and the event after now. The Hijri
  day/month pill is geometrically centered in the screen regardless of either
  prayer label's width. The side labels adapt; the center never shifts.
- The pill uses the strip's exact fill, rises above its top edge, and has its
  lower portion visually tucked into the strip so both read as one surface.
- Scrolling progressively fades the expanded sky content. The compact Azkry
  wordmark, actions, and tab row then pin at the top.
- The countdown uses the warm-yellow accent. Content cards use soft 26-point
  corners, quiet hairline borders, and restrained iconography.
- Accessibility sizes may reflow fixed reference geometry, but content may not
  overlap, clip, or become unreachable. Every interactive target is at least
  44 by 44 points on iOS and 48 by 48 dp on Android.
- Generic dashboard tiles, arbitrary gradients, and decorative icon clutter do
  not satisfy the contract.
