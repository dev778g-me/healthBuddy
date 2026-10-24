# HealthBuddy

A barrier-aware companion for medication adherence and health-seeking behaviour.

> Every reminder app tells you **when** to take your medicine.
> HealthBuddy asks **why** you didn't — and gives you something useful to do about it.

Android · Jetpack Compose · Material 3 Expressive · no backend, no network, no login.

---

## Run it

```bash
# from the healthBuddy/ folder
./gradlew :app:assembleDebug        # build only
./gradlew :app:installDebug         # build + install on a running emulator/device
```

Or just hit ▶ in Android Studio.

Min SDK 31 (Android 12) · target SDK 37.

---

## What is actually built

| Screen | What it does |
|---|---|
| **Onboarding** | Name + language (EN / हिंदी / मराठी / ଓଡ଼ିଆ). Two questions, nothing else. |
| **Today** | Needs-attention card, today's doses, next appointment, health tip |
| **Medicines** | Add, edit and toggle medicines and reminder times; the **+** FAB opens the add form as a bottom sheet, tapping a row reopens it prefilled; swipe left to remove |
| **History** | The last 30 days of doses, their barrier, whether the help was useful (answerable later), this week's barrier counts, and a one-tap **Share summary** for a clinician |
| **Learn** | Six plain-language cards |
| **Why this matters** | The loop, the research question, the safety stance |
| **Settings** | Language, theme, private mode, replay demo, reset |

## The loop

```
REMIND      a dose comes due
  ↓
LOG         Taken / Missed / Not sure
  ↓
IDENTIFY    "What made it difficult today?" — 9 one-tap options
  ↓
SUPPORT     a reviewed, barrier-specific next step
  ↓
FOLLOW UP   "Was this helpful?" → next safe step
```

All four steps live in **one** `DoseFlowSheet` with internal step state. Four separate
sheets is the classic way to lose the user (and the demo) mid-flow.

---

## Manual test checklist

Run these in order. Each maps to a PRD acceptance criterion.

1. **Cold start** → onboarding appears. Enter a name, pick हिंदी, tap Start.
   → UI is in Hindi, dashboard is populated.
2. **Settings → Language → English** → UI switches instantly, no restart.
3. **Settings → Language → ଓଡ଼ିଆ** → chrome is in Odia; barrier and education cards
   stay in English by design (unreviewed clinical copy is never machine-translated).
4. **Needs attention** card is visible (yesterday's Metformin was seeded as missed).
5. Tap it → *"What made it difficult today?"* appears with 9 options. The phone gives
   a confirm haptic.
6. Tap **"It costs too much"** → support card with body, care bullets, amber safety note.
7. Tap **Done** → *"Was this helpful?"* → tap **Yes**.
8. Close the sheet → a snackbar confirms what was logged; the dashboard card is gone.
9. **History** → the entry shows `Barrier: It costs too much` and `Helpful: Yes`, and the
   weekly card at the top counts that barrier. It must contain no advice or score.
   An entry with a barrier but no answer shows **Yes / Not sure / No** chips; tapping one
   saves it without opening the flow.
   Tap **Share summary** → the Android share sheet opens with a plain-text 30-day
   summary (counts, barriers, medicines). No permission prompt, no network.
10. **Settings → Replay demo scenario** → resets to Ravi's story **and** opens the missed
   dose straight at the barrier question.
11. **Kill the app from recents, relaunch** → all data is still there.
12. **Settings → Private mode** on → medicine names are hidden on Today.
13. **Settings → Reset all data** → confirm → onboarding returns.
14. **Medicines** → tap **+**; the add form rises as a bottom sheet over the list (no new
    screen, no back arrow). Save adds the medicine and the sheet closes.
    Tap an existing row → the same sheet opens **prefilled** and titled "Edit medicine";
    saving updates the row instead of adding a duplicate. The switch still toggles active.
15. Swipe a medicine row left; it reveals a red **Remove** action and deletes on
    release. There is no delete icon on the row.

### Safety checks (the ones that matter)

16. Open the **COST** card → it must say *"Never stop or change a prescribed medicine on your own."*
17. Open the **SIDE EFFECTS** card → it must never suggest stopping the medicine.
18. Open the **DISTRESS** card → it must offer `Call 14416`, and dialling must use the
    system dialer (no permission required, no call placed automatically).
19. Choose **"I'd rather not say"** → the app must not nag, badge, or re-ask.

---

## Architecture (deliberately boring)

```
app/src/main/java/org/dev/healthbuddy/
├── MainActivity.kt
├── data/       Store.kt (SharedPreferences + org.json) · AppState.kt (state + mutations)
├── domain/     Models · BarrierCatalog ★ · Education · Seed
└── ui/         App.kt · nav/Routes.kt · theme/ · components/ · screens/ · flow/DoseFlowSheet.kt ★
```

- **Navigation Compose 2.10.2** with type-safe `@Serializable` destinations — not a
  hand-rolled `when`. The back stack, predictive back and screen transitions come from
  the library.
- **No database** — the whole state is one JSON blob. The dataset is tiny.
- **No DI framework** — one `AppState` held by an `AndroidViewModel`.
- **No annotation processors** — AGP 9 uses built-in Kotlin. The serialization compiler
  plugin is the only addition, and type-safe routes need it.
- **Material 3 Expressive** via Compose BOM 2026.09.00 (`material3:1.5.0-beta02`), which
  supplies the `MotionScheme` used for screen transitions.

### Icon

The launcher icon is a single adaptive icon (`res/mipmap-anydpi/ic_launcher.xml` and
`ic_launcher_round.xml`) built from three vectors: foreground (cream heart + capsule),
background gradient, and a monochrome layer for Android 13+ themed icons. `minSdk 31`
means `mipmap-anydpi` always wins, so there are no density-specific bitmaps. Editable
sources and the 512 px store art live in `branding/` and are not packaged into the APK.

### Content is data, not code paths

The nine barrier responses live in `BarrierCatalog.kt` as reviewed constants
(English + Hindi). They are never generated at runtime. Every card obeys the
safety rules in `PRD.md` §9.

---

## Deliberately not built

Login · backend · sync · AI advice · push notifications · telemedicine · maps · community ·
caregivers · streaks and leaderboards.

Streaks in particular were rejected: punishing a user for a missed dose contradicts the
entire premise. A missed dose is information, not failure — it is coloured by the theme's
error role and never turned into a score or a scolding.

---

## Docs

- `PRD.md` — full product requirements, safety rules, content spec, demo script.
- `docs/one-slide.svg` — 16:9 architecture + loop slide, drop-in ready.
- `docs/one-slide.md` — the same content as text, plus presenter notes.
- `branding/` — icon vectors, 512 px store art, and how the adaptive icon is wired.
