# How HealthBuddy Works

A plain-English guide. No tech talk.

## What the app does

HealthBuddy remembers your medicines for you. Add them once, tell it when to
remind you, and it nags you at the right time — even if you restart the phone.

**The 4 tabs:**

- **Today** — what to take right now, and what you already took
- **Medicines** — your list; add, edit, or pause things
- **History** — a log of what you took, missed, or skipped (with reasons)
- **Learn** — short articles about why doses matter, missed doses, etc.

## The cool part: scan a paper prescription

You know those paper prescriptions from the doctor? The app can read them:

1. Tap the **+ button** on Medicines → **Scan prescription**
2. Take a photo (or pick one from your gallery)
3. The app reads the handwriting and prints, then shows you everything it
   found — medicine name, dose, how many times a day, what times, for how long
4. **You check it.** Nothing is saved until you tick the boxes and press Save

That check step matters. If the app misread something, you fix it *before* it
starts reminding you.

## What happens behind the scenes

The scan runs in two stages:

**Stage 1 — reading the photo (happens on your phone)**

Your phone's own text-reader (Google's ML Kit) looks at the photo and turns it
into text. This part needs **no internet**. The photo stays on your phone.

**Stage 2 — making sense of it (the AI)**

The text (only the text — never the photo) is sent to an AI model called
**GPT-OSS 120B**, which figures out which lines are medicines and what they
mean. It's good at messy handwriting and abbreviations like "1-0-1" or "BD".

You can switch this AI step off in the scan screen. If you do — or if the AI
fails — the app falls back to a simpler built-in reader. You'll always see a
warning telling you which one was used.

Whatever happens, **the photo is deleted right after the scan.**

## Your reminders work offline

Once a medicine is saved, the reminders are handled by your phone itself.
No internet needed. The app also checks after a restart, so reminders survive
reboots.

## What we don't do

- ❌ No ads
- ❌ No accounts or sign-ups
- ❌ No analytics tracking you
- ❌ The photo is never stored or shared — it's deleted immediately
- ❌ Only extracted text goes to the AI, and only if you enable it

## One honest warning

HealthBuddy is a reminder tool, **not a doctor**. It never suggests medicines
or doses — it only reminds you about what *your* doctor prescribed. Always
follow the actual prescription.
