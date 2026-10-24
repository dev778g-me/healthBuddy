# One-slide architecture + loop

Two exports of the same content: `one-slide.svg` (drop straight into Slides / Figma /
Keynote, 16:9) and this outline if you'd rather rebuild it in the deck's own theme.

## Content for the slide

**Title** — HealthBuddy
**Subtitle** — Every reminder app tells you *when*. HealthBuddy asks *why* you didn't.

### Left: the loop (5 steps, one bottom sheet, ≤ 6 taps)

1. **REMIND** — a dose comes due
2. **LOG** — Taken / Missed / Not sure
3. **IDENTIFY** — "What made it difficult today?" — 10 one-tap barriers
4. **SUPPORT** — a reviewed, barrier-specific next step
5. **FOLLOW UP** — "Was this helpful?" → next safe step

Footnote: no dead ends, no score, no streak, no scolding.

### Right: architecture (deliberately boring)

1. **Compose UI** — one `Scaffold`, one animated top bar, type-safe routes
2. **AppState** — a single observable holder; every mutation in one place
3. **Store** — one versioned JSON blob in `SharedPreferences`; no DB, no DI, no network

### Right: why it's different

- Asks **why**, not just when — 10 barrier options, one tap each
- Answers with a **reviewed next step**, never a score or streak
- **Fully offline**: no login, no backend, no permissions
- **Escalates safely**: 14416 for distress, 104 for cost/access

**Footer** — Material 3 Expressive · EN / हिंदी / मराठी / ଓଡ଼ିଆ · `org.dev.healthbuddy`

## Presenter notes

- One sentence on the research question: *does asking about the barrier, and answering it,
  change what the user does next?*
- The demo is the argument: play the missed dose from §7, don't describe it.
- If asked "is this medical advice?" — no: all 10 responses are reviewed constants, and
  every card ends in a next step or a helpline, never a diagnosis or a dose change.
