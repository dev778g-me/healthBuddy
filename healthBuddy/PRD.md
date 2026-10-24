# PRD — HealthBuddy

**A barrier-aware companion for medication adherence and health-seeking behaviour**

|              |                                                                                                  |
| ------------ | ------------------------------------------------------------------------------------------------ |
| Version      | 1.0                                                                                              |
| Status       | Approved for build                                                                               |
| Platform     | Android · Jetpack Compose · Material 3 Expressive                                                |
| Build target | Hackathon MVP, demo-ready                                                                        |
| Source docs  | `../MyHealthBuddy.docx`, `HealthMate App.docx`, `Poor adherence to medication and poor health.docx` |

---

## 1. One-line pitch

> Every reminder app tells you **when** to take your medicine. MyHealthBuddy asks **why** you didn't — and gives you something useful to do about it.

Say this to the judges. It is the entire product.

---

## 2. Problem

Chronic conditions — diabetes, hypertension, psychiatric, neurological, rheumatic — require years of consistent medication and timely consultation. Patients miss doses and delay care for reasons that have nothing to do with laziness:

forgetfulness · cost · side effects · not understanding instructions · stigma · distance from facilities · emotional distress · low health literacy · language barriers · complex regimens · weak social support.

**The gap in existing tools.** A reminder assumes the cause of a missed dose is forgetting. When the real cause is that the medicine costs ₹800 and the patient earns ₹9,000 a month, the app fires the same notification again tomorrow. The barrier is never named, so it is never solved.

**India specificity.** High out-of-pocket expenditure, patchy insurance coverage, rural access gaps, under-resourced facilities with little follow-up, and stigma around mental health all compound the problem.

### 2.1 Research question (state this to judges)

> Can a barrier-aware mobile application provide more relevant and useful support for medication adherence and health-seeking behaviour than a reminder-only approach?

The app is the hypothesis made tangible. Do not claim clinical effectiveness — claim a testable model with a working demonstration.

---

## 3. Goal of this MVP

**Primary goal:** a working, demoable app that proves the barrier loop is real, usable, and safe — in under 90 seconds of stage time.

**Secondary goals**
1. Look like a real product, not a prototype (theme, icons, animation, dark mode).
2. Never show a broken state on stage (offline, no login, deterministic demo path).
3. Be medically defensible — every response routes to a human, no response advises changing treatment.

**Explicit non-goal:** proving adherence improves. That requires a study, not a hackathon.

---

## 4. The core loop

```
   REMIND          dashboard shows a dose is due
      ↓
   LOG             user taps Taken / Missed / Not sure
      ↓
   IDENTIFY        "What made it difficult today?"  (one tap, 10 options)
      ↓
   SUPPORT         barrier-specific card + one suggested next action
      ↓
   FOLLOW UP       "Was this helpful?" → next safe step
```

Everything else in the app exists to support this loop.

---

## 5. Success criteria for the MVP

| # | Criterion | Test |
|---|---|---|
| S1 | The full loop completes in ≤ 6 taps | Manual, on emulator |
| S2 | Zero unsafe advice in the catalog | Every card reviewed against §9 rules |
| S3 | App survives force-close and restart with data intact | Kill + relaunch |
| S4 | Demo scenario button reproduces the cost story every time | Press 5×, identical result |
| S5 | Works with no network | Airplane mode walkthrough |
| S6 | Readable one-handed on a 6" screen | Emulator Pixel 7 |

---

## 6. Users

| Persona | Context | What they need from the MVP |
|---|---|---|
| **Ravi, 54** — hypertension + type 2 diabetes, semi-urban | Shared family phone, moderate literacy, cost-sensitive | Big buttons, clear schedule, cost help that isn't a lecture |
| **Meera, 29** — on psychiatric medication, urban | Fears being seen; highly sensitive to judgement | Private notifications, no diagnosis on the lock screen, zero shaming copy |
| **Anjali, 41** — caregiver for her father | Manages someone else's medicines | Simple overview of what's due and what was missed |
| **Dr. Rao, PHC physician** (indirect stakeholder) | Skeptical of apps | Confidence the app never contradicts his instructions |

**Demo persona:** Ravi. All seeded data and the demo scenario use Ravi's story, because cost is the most legible barrier to an audience in 20 seconds.

---

## 7. Demo script (rehearse this)

| Time | Beat |
|---|---|
| 0:00 | Open app → dashboard. "Ravi has hypertension and diabetes. Three medicines today." |
| 0:10 | Last night's Metformin shows **Missed → needs help**. "A normal reminder app stops here." |
| 0:18 | Tap it → *"What made it difficult today?"* → tap **"It costs too much."** |
| 0:28 | Support card appears. Read the first line aloud. "Notice what it did **not** say — it did not say stop taking it." |
| 0:40 | Read the care options aloud (PHC · Jan Aushadhi · helpline 104) → tap **Was this helpful? Yes** |
| 0:48 | Dashboard updates: *barrier identified: cost*. "Now it knows something a reminder app never could." |
| 0:55 | Open **Why this matters** → the loop diagram + research question. |
| 1:05 | **Demo scenario** → resets and reopens the missed dose at the barrier question. "One tap, back to the start." |

---

## 8. Scope

### 8.1 In scope (build this)

- 7 screens: Onboarding, Dashboard, Medicines, History, Learn, Why it matters, Settings
- The barrier flow in one bottom sheet (log → barrier → support → follow-up)
- Today progress count (taken / total) — no score, no streak
- Optional note on any dose, included in the shared visit summary
- Local dose reminders at each medicine time (`AlarmManager`, offline, no push)
- Onboarding permission gate for notifications + exact alarms
- Material 3 time picker for custom reminder times
- Local persistence
- 4 languages: English, हिंदी, मराठी, ଓଡ଼ିଆ (UI strings)
- Demo scenario mode
- Light + dark theme
- Support content: English + Hindi, both reviewed; Marathi and Odia fall back to English (see §11.3)

### 8.2 Out of scope (do NOT build)

Do not build these even if there is time. Each one is a demo risk or a safety risk.

| Not building | Why not |
|---|---|
| Login / accounts / OTP | Adds friction to every demo run |
| Backend, API, cloud sync | Nothing to demo; everything to break |
| AI / LLM-generated advice | Unsafe, unreviewable, and the point is *curated* responses |
| Push/FCM notifications | Remote push needs a backend. **Local** `AlarmManager` reminders are in scope (§12.1) |
| Telemedicine, video calls | Partner dependency |
| Maps, GPS, "doctors near me" with real data | Unverified data is a safety liability. Use a static curated list |
| Community forum / chat | Needs moderation; empty forum looks bad |
| Caregiver accounts | Multi-user state explosion |
| Gamification: streaks, points, leaderboards | **Directly contradicts the non-judgmental stance.** A streak punishes the exact user we're designing for |
| Insurance / claims / ABHA integration | Regulatory and scope creep |
| iOS App Store build | Code is shared; don't spend time on signing |
| Symptom analytics / ML insights | Nice screenshot, no substance |
| Appointments syncing with calendar providers | Platform-specific rabbit hole |

If a judge asks about any of the above, answer: *"Designed, deliberately deferred — here's the roadmap."* There is a roadmap section (§14) precisely for this.

---

## 9. Safety rules (hard constraints, non-negotiable)

These are enforced in content, not in code, but they constrain every string written.

1. **Never** advise starting, stopping, switching, or substituting a medicine.
2. **Never** suggest a dose or dose change.
3. **Never** diagnose or interpret a symptom as a condition.
4. **Never** imply the app replaces a clinician.
5. **Always** provide a route to a human professional where clinically relevant.
6. **Always** include a safety note on cards for COST and SIDE_EFFECTS.
7. Red-flag symptoms (breathing difficulty, swelling, chest pain, severe dizziness, suicidal ideation) route to **emergency/helpline**, never to self-management.
8. "Prefer not to answer" is always available and never penalised, nagged, or re-asked.
9. No copy that shames, scolds, or implies failure.

**Review gate:** every support card in §10 must pass a read-through against these 9 rules before the code is written. This is the PRD author's job, done now.

---

## 10. Content specification — barrier catalog

This is the product's actual IP. Ten entries. Each is a data class, not a screen.

### 10.1 Schema

```kotlin
data class BarrierSupport(
    val barrier: Barrier,
    val label: String,           // shown as the option in the question
    val title: String,           // support card headline
    val body: String,            // 2–3 sentences, plain language
    val actionLabel: String?,    // primary button
    val actionType: ActionType,  // ADJUST_REMINDER, FIND_FACILITY, CALL, READ, PRIVATE_MODE, NONE
    val escalation: Escalation,  // NONE, CLINICIAN, CRISIS
    val safetyNote: String?      // shown in a tinted box
)
```

### 10.2 The ten barriers

**1. `FORGOT` — "I simply forgot"**
- Title: *Forgetting is normal — let's make it easier*
- Body: Most doses are missed when the timing doesn't fit the day. We can move this reminder to a moment that works better — after breakfast, before bed, whenever you're actually home.
- Action: **Move this reminder** → `ADJUST_REMINDER` (chips: +15 / +30 / +60 min)
- Escalation: none
- Safety: —

**2. `COST` — "It costs too much"** ★ *the flagship card*
- Title: *Cost is a real barrier — and there is help*
- Body: Medicines costing more than expected is one of the most common reasons doses get missed. This is not your fault. Your prescriber or pharmacist can tell you about cheaper options, generic equivalents, or government schemes you may qualify for.
- Action: **Find care options** → `FIND_FACILITY` (labelled list + Call 104)
- Escalation: clinician
- Safety: *Never stop or change a prescribed medicine on your own. Speak to your doctor or pharmacist first.*

**3. `STOCK_OUT` — "I ran out of my medicine"**
- Title: *Running out is a supply problem, not a failure*
- Body: Strips finish sooner than you expect — a refill was missed, the shop had none, or the week got busy. This happens to most people. A nearby PHC or Jan Aushadhi Kendra may be able to supply the same prescribed medicine, and 104 can tell you what is open near you.
- Action: **Where to get it** → `FIND_FACILITY` (labelled list + Call 104)
- Escalation: none
- Safety: *Do not take a different medicine or change your dose to make up for the missed strips. Ask your doctor or pharmacist first.*

**4. `SIDE_EFFECTS` — "I felt unwell, or was worried about side effects"**
- Title: *Side effects should be reported, not endured silently*
- Body: If a medicine is making you feel unwell — or you're worried it might — that is important information for your doctor, not something to work out alone. Note what you felt and when, and take it to your next visit.
- Action: **What to note down** → `READ`
- Escalation: clinician
- Safety: *Do not stop the medicine on your own. If you have trouble breathing, swelling, chest pain, or severe dizziness, seek urgent care now.*

**5. `CONFUSED` — "I didn't understand how to take it"**
- Title: *Let's make the instructions clear*
- Body: Instructions get confusing — different timings, before or after food, tablets that look alike. Your pharmacist can explain this in your own language in under a minute, and it's a completely normal thing to ask.
- Action: **What to ask your pharmacist** → `READ`
- Escalation: none
- Safety: —

**6. `STIGMA` — "I felt embarrassed, or worried what people would think"**
- Title: *Your treatment is private — and you're not alone*
- Body: Many people delay medicines because of what others might think. Your health information stays on this device, and you can hide medicine names from this screen whenever you need to.
- Action: **Turn on private mode** → `PRIVATE_MODE`
- Escalation: none
- Safety: —

**7. `ACCESS` — "I couldn't get to a clinic, pharmacy, or my medicine"**
- Title: *Distance shouldn't decide your treatment*
- Body: If travel or transport is getting in the way, there may be something closer — a primary health centre, a Jan Aushadhi store, or a phone consultation.
- Action: **Find something closer** → `FIND_FACILITY`
- Escalation: none
- Safety: —

**8. `DISTRESS` — "I was feeling low, anxious, or alone"**
- Title: *Looking after your mind is part of looking after your health*
- Body: When you're exhausted or overwhelmed, taking medicine can feel impossible. That is worth telling someone — a clinician, or a helpline that is free and confidential.
- Action: **Call a helpline** → `CALL` (14416)
- Escalation: crisis
- Safety: *If you are having thoughts of harming yourself, please call **14416** (Tele-MANAS, free, 24×7) or your local emergency number now.*

**9. `OTHER` — "Something else"**
- Title: *Thanks for telling us*
- Body: Whatever it was, it's worth mentioning at your next visit. Even a small difficulty is useful information for your doctor.
- Action: **What to mention** → `READ`
- Escalation: none
- Safety: —

**10. `PREFER_NOT_TO_SAY` — "I'd rather not say"**
- Title: *That's completely fine*
- Body: You don't have to explain. Your dose has been recorded and you can carry on. We'll be here if you ever want to share.
- Action: none
- Escalation: none
- Safety: —

> **Design note:** entries 9 and 10 exist so the question is never a trap. Never re-ask, never badge it, never show "you skipped the question".

### 10.3 Education content (6 cards)

| # | Title | Content |
|---|---|---|
| 1 | Why missing doses matters | Steady medicine levels vs. peaks and gaps; complications over years, not days |
| 2 | What adherence actually means | Consistency, not perfection; what to do when you do miss one |
| 3 | Paying less for the same medicine | Generics, Jan Aushadhi, asking for a cheaper equivalent *through* your doctor |
| 4 | Side effects: what to report | Which to note down, which need urgent care |
| 5 | When to see a doctor sooner | Red-flag list, plainly worded |
| 6 | Stigma and mental health | Privacy rights, that treatment is not a weakness, helpline |

Each card: ~80 words, reading level ~grade 6, one line of "this is education, not medical advice".

### 10.4 Seed data

**Medicines**
| Name | Dose | Condition | Times |
|---|---|---|---|
| Metformin | 500 mg | Type 2 diabetes | 08:00, 20:00 |
| Amlodipine | 5 mg | Hypertension | 09:00 |
| Sertraline | 50 mg | Mental health | 21:00 |

**Appointments**: Dr. Rao, PHC — 3 days out (follow-up); Dr. Nair, diabetes clinic — 12 days out.
**Tips**: 6 rotating one-liners.
**Demo log state**: yesterday's 20:00 Metformin pre-seeded as `MISSED` with no barrier identified — this is what makes the demo start immediately.

---

## 11. Requirements

### 11.1 Functional

| ID | Requirement | Acceptance |
|---|---|---|
| FR-1 | Onboarding: language, display name, and the reminder permission card | One screen; persisted; **Get started** gated on notification + alarm access (or an explicit reminder opt-out) |
| FR-2 | Dashboard shows today's doses with time and status | Correct grouping: due now / later / done |
| FR-3 | Dose can be logged as Taken, Missed, or Not sure | Status persists; history updates |
| FR-4 | Logging Missed opens the barrier question | 10 options, one tap each, dismissible |
| FR-5 | Selecting a barrier shows the support card | Copy matches §10.2 exactly; safety note visible when defined |
| FR-6 | Support card action does something visible | Adjust → +15/30/60 chips; Facility → labelled care list + 104 call; Call → dial intent; Read → labelled checklist; Privacy → turns on private mode |
| FR-7 | Follow-up asks "Was this helpful?" with 3 answers | Answer stored on the log entry |
| FR-8 | Unanswered "Was this helpful?" is recordable later | Yes/Not sure/No chips on the History entry |
| FR-9 | Medicine CRUD: add, edit, toggle active | Add flow ≤ 4 inputs; tap a row to edit it in the same sheet |
| FR-10 | History timeline of last 30 days | Windowed to today − 29 d; shows status + barrier + helpfulness |
| FR-11 | Learn list + detail | 6 cards, offline |
| FR-12 | Why it matters screen | Loop diagram, research question, safety statement |
| FR-13 | Demo scenario button | Replays §7 flow deterministically |
| FR-14 | Language switchable at runtime | EN / HI / MR / OR, no restart needed |
| FR-15 | Data persists across restart | JSON blob reload |
| FR-16 | Reset all data | In settings/menu, with confirm |
| FR-17 | Today shows how many doses were taken | "2 of 3 taken" + thin bar; counts only, hidden when no doses are due |
| FR-18 | Optional note on any logged dose | Editable from History, 140 chars, included in the shared summary; private mode leaves the medicine name out |
| FR-19 | Local reminder at each medicine time | One exact alarm per medicine × time; notification opens Today; private mode hides the medicine name and the lock screen shows a generic public version |
| FR-20 | Onboarding asks for notification + alarm access | Two explicit permission rows; **Get started** stays disabled until both are granted (turning reminders off is an explicit opt-out) |
| FR-21 | Custom reminder time uses a time picker | Material 3 `TimePicker` dialog, 12-hour with AM/PM, no free-text format errors |

### 11.2 Non-functional

- Cold start < 2 s on emulator
- No network calls at all in v1
- No crash on empty states (no medicines, no history)
- Text ≥ 14sp body, ≥ 16sp primary buttons, tap targets ≥ 48dp
- All strings in the `Strings` layer — no hardcoded text in composables

### 11.3 Localization policy

- **UI chrome** (buttons, labels, nav, onboarding, question options): EN / HI / MR / OR.
- **Support card bodies, education content and tips:** EN + HI, both reviewed.
  - Marathi and Odia fall back to English for that content. Machine-translating clinical
    copy without review violates §9, so unreviewed clinical copy in those languages is
    deliberately not shipped — one reviewed language beats four unreviewed ones.
  - Odia chrome is UI text only and has not had a native-speaker review; a native pass is
    recommended before showing it to Odia-speaking users.
  - Say this out loud if asked — it is a *better* answer than "we ran it through Google Translate".

---

## 12. Technical design

### 12.1 Stack

| Concern | Decision | Rationale |
|---|---|---|
| UI | Jetpack Compose, single Activity | One module, no fragments |
| Design system | **Material 3 Expressive** (`androidx.compose.material3` 1.5.0-beta02, via Compose BOM 2026.09.00) | Expressive type scale, shape tokens and `MotionScheme` (nav transitions use `defaultSpatialSpec` / `fastSpatialSpec`). The BOM pins Compose 1.12.1, which satisfies material3's 1.12.0 floor |
| Navigation | **Navigation Compose 2.10.2** with type-safe `@Serializable` destinations, plus a `Tab` enum for the four main tabs | One `NavHost` owns the back stack, predictive-back transitions and per-destination chrome. Tabs are state inside `MainRoute`, not destinations |
| Persistence | One JSON blob in `SharedPreferences`, built with `org.json` | Zero dependencies. No Room/KSP/kapt — critical because AGP 9 uses built-in Kotlin and annotation processors are friction |
| Time | `java.time` (minSdk 31) | No kotlinx-datetime needed |
| State | `mutableStateOf` inside one `AppState` holder, kept by an `AndroidViewModel` | Survives rotation/config changes. No DI library |
| Icons | **Lucide** (`com.composables:icons-lucide` 1.1.0) | One consistent 24 px outline family instead of mixed Material glyphs. The `thelacspace:lucide-compose` artifact was evaluated and rejected: its 1.16.0 AAR ships only four compiled icons |
| Notifications | **Local only** — one exact `AlarmManager` alarm per active medicine × time, re-armed by a receiver; no push, no server | Works in airplane mode and needs no backend. Exact alarms fall back to inexact if the phone refuses the permission |

### 12.2 Project structure

```
app/src/main/java/org/dev/healthbuddy/
├── MainActivity.kt               creates Store + ReminderScheduler + AppState, setContent
├── notifications/                ReminderScheduler (exact alarms + channel), DoseAlarmReceiver,
│                                 BootReceiver, ReminderPermissions
├── data/
│   ├── Store.kt                  SharedPreferences + org.json encode/decode
│   └── AppState.kt               observable state + all mutations
├── domain/
│   ├── Models.kt                 Medicine, DoseLog, DoseSlot, Tab, enums
│   ├── BarrierCatalog.kt         ★ the 10 reviewed support cards (EN + HI)
│   ├── Education.kt              6 learning cards (EN + HI)
│   └── Seed.kt                   Ravi's demo data + rotating tips
└── ui/
    ├── App.kt                    root: onboarding vs shell, ONE Scaffold + animated top bar,
    │                             NavHost, FAB, snackbar, sheet host
    ├── nav/Routes.kt             @Serializable destinations (Main, Settings,
    │                             LearnDetail, WhyMatters)
    ├── Format.kt                 time / relative-date formatting
    ├── i18n/Strings.kt           EN / HI / MR / OR chrome + LocalStrings
    ├── theme/                    Color.kt, Type.kt (expressive scale), Theme.kt
    ├── components/               HbCard, StatusPill, SafetyNote, OptionRow, EmptyState…
    ├── screens/                  Onboarding, Dashboard, Medicines (+ AddMedicineSheet),
    │                             History, Learn, LearnDetail, WhyMatters, Settings
    └── flow/DoseFlowSheet.kt     ★ the whole loop in one bottom sheet

app/src/main/res/
├── drawable/                     ic_launcher_{foreground,background,monochrome}.xml, onboarding glyph,
│                                 ic_notification.xml
├── mipmap-anydpi/                <adaptive-icon> wrappers (square + round); no density bitmaps, minSdk 31
├── values/, values-night/        theme, app name
└── xml/                          backup / data-extraction rules

branding/                         icon vectors, 512 px store art and notes (source only, not packaged)
docs/                             one-slide.svg + one-slide.md (architecture, loop, presenter notes)
```

### 12.3 Data model

```kotlin
enum class Barrier { FORGOT, COST, STOCK_OUT, SIDE_EFFECTS, CONFUSED, STIGMA, ACCESS, DISTRESS, OTHER, PREFER_NOT_TO_SAY }
enum class DoseStatus { PENDING, TAKEN, MISSED, UNSURE }
enum class Helpful { YES, NOT_SURE, NO }
enum class Lang { EN, HI, MR, OR }

data class Medicine(id, name, dose, condition, times: List<String>, active: Boolean, reminderOffsetMinutes: Int)
data class DoseLog(id, medicineId, date, time, status, barrier: Barrier?, helpful: Helpful?, note: String?, loggedAt: Long)
data class DoseSlot(medicine, date, time, baseTime, log)   // id = medicineId|date|baseTime

data class BarrierSupport(barrier, action: ActionType, escalation: Escalation, callNumber, en: BarrierText, hi: BarrierText)
```

Dates and times are stored as strings (`yyyy-MM-dd`, `HH:mm`) — the dataset is tiny and string comparison sorts correctly, which removes an entire class of serialization bugs.

`Snapshot` also stores `remindersEnabled` (default on), so the Settings toggle survives a restart. Local alarms themselves live in `AlarmManager`, not in the JSON blob.

`reminderOffsetMinutes` exists so the "I forgot" support actually *does* something: moving the reminder shifts the displayed time without changing the dose's identity (`baseTime` drives the log id).

### 12.4 Design direction

- **Palette:** calm teal-green primary (`#1F6F5C`), warm sand background, status colours from the theme: success (green) for a logged dose, error for a missed one, both following the light/dark preference. The copy stays non-shaming — no scores, streaks or blame.
- **Type:** system font, generous line height, one accent weight.
- **Shape:** 20dp rounded cards, 28dp sheets, no hard borders.
- **Motion:** bottom sheets slide, support card fades in with a 150ms rise. Nothing bouncy.
- **Tone in copy:** second person, calm, zero exclamation marks.

---

## 13. Task breakdown

Sizes: **S** ≈ <30 min, **M** ≈ 1–2 h, **L** ≈ 3–4 h.

### Phase 0 — Scaffold (blocker)

- [x] Project scaffolded: `healthBuddy/`, package `org.dev.healthbuddy`, minSdk 31, targetSdk 37 **(done)**
- [x] Dependencies: Material 3 Expressive 1.5.0-beta02 (BOM 2026.09.00), Lucide icons 1.1.0 (`material-icons-core` removed), Navigation Compose 2.10.2 + kotlinx-serialization (needed for type-safe routes) **(done)**
- [x] Verified `./gradlew :app:installDebug` on device — builds and runs **(done)**
- [x] App name set to "HealthBuddy" **(done)**

### Phase 1 — Domain (no UI)

- [x] `Models.kt` — enums + data classes **(S)**
- [x] `BarrierCatalog.kt` — all 10 entries, copy verbatim from §10.2 **(L)**
- [x] `Education.kt` — 6 cards **(M)**
- [x] `Seed.kt` — medicines, appointments, tips, pre-seeded missed dose **(S)**
- [x] `Repository.kt` — JSON read/write, schema version **(M)**
- [x] `AppState.kt` — state + actions (logDose, setBarrier, setHelpful, addMedicine…) **(L)**

### Phase 2 — The money flow ★

- [x] Theme + `App.kt` shell + navigation **(M)**
- [x] `DashboardScreen` — today's doses, due-now section, appointment, tip **(L)**
- [x] Dose log → barrier → support → follow-up — shipped as **one** `DoseFlowSheet` with internal step state (four separate sheets kept losing the user mid-flow) **(L)**
- [x] Wire end-to-end; verify ≤ 6 taps **(M)**

### Phase 3 — Supporting screens

- [x] `OnboardingScreen` — language, name, reminder permission gate **(M)**
- [x] `MedicinesScreen` + add/edit-medicine **bottom sheet** (no route) — tap a row to edit, swipe left to remove; no always-visible delete icon **(L)**
- [x] `HistoryScreen` — day-grouped timeline with barrier chips **(M)**
- [x] `LearnScreen` + detail **(M)**
- [x] `WhyItMattersScreen` — loop drawn in Compose **(M)**
- [x] `SettingsScreen` — language, theme, private mode, demo replay, reset **(S)**

### Phase 4 — Demo armour

- [x] Persistence round-trip — `Store` writes a versioned JSON snapshot on every mutation **(S)**
- [x] **Demo scenario** button — resets to Ravi state **and** opens the missed-dose flow at the barrier question **(M)**
- [x] HI + MR + OR strings for all UI chrome **(L)**
- [x] Empty states (no medicines, no history, no logs today) **(S)**
- [x] Answer a skipped "Was this helpful?" from the History entry **(S)**
- [x] Dark mode **(M)**
- [x] Launcher icon — adaptive foreground/background + Android 13 monochrome (themed) layer; `minSdk 31` so no legacy density bitmaps; system splash reuses the icon **(done)**
- [ ] Rehearse §7 three times, time it **(S)** — presenter's job

### Phase 5 — Optional, only if Phase 4 is done

- [x] Local notification via `AlarmManager` — one exact alarm per active medicine × time, boot receiver re-arms, tap opens Today **(M)**
- [x] Onboarding permission gate — notifications + "Alarms & reminders", Continue disabled until granted **(S)**
- [x] Custom time entry replaced by the Material 3 `TimePicker` dialog **(S)**
- [x] Weekly "what got in the way" summary card on History — counts only, no advice **(M)**
- [x] **Share summary** on History — last 30 days of counts, barriers and medicines as plain text through the Android share sheet; no permission, no network, no advice **(S)**
- [x] Haptic feedback (`HapticFeedbackType.Confirm`) + snackbar confirmation on log **(S)**

### Phase 6 — Documentation for submission

- [x] `README.md` — what it does, how to run, the research question, safety model **(M)**
- [ ] Screenshots for the report/poster **(S)** — needs a running build
- [x] One-slide architecture + loop diagram export — `docs/one-slide.svg` (16:9) plus text/notes in `docs/one-slide.md` **(done)**

---

## 14. Roadmap (for judge questions, not for building)

| Feature | Why deferred |
|---|---|
| Regional-language + voice interface | Highest-impact accessibility work; needs reviewed translation |
| Offline-first education packs | Partly covered already, needs content pipeline |
| Caregiver sharing (consented) | High demand, multi-user complexity |
| Verified medicine availability + price | Requires a verified data source; stale data is unsafe |
| Provider dashboard (consented) | Needs consent model + clinical sign-off |
| Teleconsultation | Partner dependency |
| Validated AI barrier-pattern analysis | Only after clinical validation; never user-facing advice |
| Formal evaluation study | The actual next step: fictional-scenario scoring vs. reminder-only baseline |

---

## 15. Risks

| # | Risk | Mitigation |
|---|---|---|
| R1 | Support card reads as medical advice | §9 rules + fixed reviewed copy + safety notes |
| R2 | Demo fails live | No network, no login, deterministic demo button, data seeded |
| R3 | Barrier question feels like interrogation | One tap, skippable, "prefer not to say" first-class |
| R4 | Judges ask "does this actually work?" | Answer with the research question + evaluation design, never with a claim |
| R5 | Dependency/version pain | Minimal deps: no DI, no database, no network. Navigation Compose + kotlinx-serialization are the only additions, pinned and cached |
| R6 | Translation quality challenged | Documented policy in §11.3 |
| R7 | Looks like a student project | Theme, icon, motion, dark mode — Phase 4 is not optional |

---

## 16. Definition of done

The MVP is done when **all** of these are true:

1. Fresh install → onboarding → dashboard → log a missed dose → barrier → support → follow-up → history, with no dead ends.
2. Demo scenario replays §7 exactly, three times in a row.
3. App restart preserves all state.
4. Airplane mode changes nothing.
5. Every support card matches §10.2 word-for-word.
6. Status colours come from the theme — success for logged, error for missed — and no copy blames the user.
7. §7 rehearsed under 90 seconds.
8. A medicine set two minutes ahead fires a local notification on time, tapping it opens Today, and private mode keeps the name off the lock screen.

---

## 17. Open questions

| # | Question | Default if unanswered |
|---|---|---|
| Q1 | Product name on the launcher: "MyHealthBuddy" or "HealthMate"? | **HealthBuddy** — matches the package and the launcher label |
| Q2 | Target set: Android only, or Android + iOS + Desktop? | **Android only** |
| Q3 | Real local notifications in scope? | **Yes** — local `AlarmManager` only; remote push stays out |
| Q4 | Hindi + Marathi + Odia, or Hindi only? | **Chrome in EN/HI/MR/OR; clinical content EN/HI** — Marathi and Odia clinical copy is not reviewed, so it falls back |
| Q5 | Neurological condition in seed data too? | **No** — three conditions is enough for the demo |
