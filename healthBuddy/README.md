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
| **Onboarding** | Name + language (EN / हिंदी / मराठी). Two questions, nothing else. |
| **Today** | Needs-attention card, today's doses, next appointment, health tip |
| **Medicines** | Add / toggle / remove medicines and reminder times |
| **History** | Every dose, its barrier, and whether the help was useful |
| **Learn** | Six plain-language cards |
| **Why this matters** | The loop, the research question, the safety stance |
| **Settings** | Language, private mode, replay demo, reset |

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
3. **Needs attention** card is visible (yesterday's Metformin was seeded as missed).
4. Tap it → *"What made it difficult today?"* appears with 9 options.
5. Tap **"It costs too much"** → support card with body, care bullets, amber safety note.
6. Tap **Done** → *"Was this helpful?"* → tap **Yes**.
7. Close the sheet → dashboard no longer shows that card as needing attention.
8. **History** → the entry shows `Barrier: It costs too much` and `Helpful: Yes`.
9. **Settings → Replay demo scenario** → everything resets to Ravi's story.
10. **Kill the app from recents, relaunch** → all data is still there.
11. **Settings → Private mode** on → medicine names are hidden on Today.
12. **Settings → Reset all data** → confirm → onboarding returns.

### Safety checks (the ones that matter)

13. Open the **COST** card → it must say *"Never stop or change a prescribed medicine on your own."*
14. Open the **SIDE EFFECTS** card → it must never suggest stopping the medicine.
15. Open the **DISTRESS** card → it must offer `Call 14416`, and dialling must use the
    system dialer (no permission required, no call placed automatically).
16. Choose **"I'd rather not say"** → the app must not nag, badge, or re-ask.

---

## Architecture (deliberately boring)

```
app/src/main/java/org/dev/healthbuddy/
├── MainActivity.kt
├── data/       Store.kt (SharedPreferences + org.json) · AppState.kt (state + mutations)
├── domain/     Models · BarrierCatalog ★ · Education · Seed
└── ui/         App.kt · theme/ · components/ · screens/ · flow/DoseFlowSheet.kt ★
```

- **No navigation library** — a sealed `Route` and a `Tab` enum, resolved by `when`.
- **No database** — the whole state is one JSON blob. The dataset is tiny.
- **No DI framework** — one `AppState` constructed in `MainActivity`.
- **No annotation processors** — AGP 9 uses built-in Kotlin; KSP/kapt would be friction.
- **Material 3 Expressive**, pinned to `material3:1.4.0` (the first stable release with
  the Expressive type scale and shape tokens). No betas.

### Content is data, not code paths

The nine barrier responses live in `BarrierCatalog.kt` as reviewed constants
(English + Hindi). They are never generated at runtime. Every card obeys the
safety rules in `PRD.md` §9.

---

## Deliberately not built

Login · backend · sync · AI advice · push notifications · telemedicine · maps · community ·
caregivers · streaks and leaderboards.

Streaks in particular were rejected: punishing a user for a missed dose contradicts the
entire premise. A missed dose is information, not failure — and it is never shown in red.

---

## Docs

- `PRD.md` — full product requirements, safety rules, content spec, demo script.
