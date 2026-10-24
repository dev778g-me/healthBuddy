# HealthBuddy — full test plan & demo recording script

Build: `./gradlew :app:installDebug` · Min SDK 31 (Android 12) · target SDK 37
Package: `org.dev.healthbuddy`

Tick every box on a **clean install**, top to bottom. If something fails, record the ID
(`NTF-04`), the device, and what you saw — not just "not working".

---

## 0. Pre-flight

- [x] **0.1** Build + install: `cd healthBuddy && ./gradlew :app:installDebug`
- [x] **0.2** Clean state: `adb shell pm clear org.dev.healthbuddy`
      (or uninstall + reinstall; this removes onboarding and permissions)
- [x] **0.3** Phone unlocked, Do Not Disturb **off**, media volume up
- [x] **0.4** Battery: Settings → Apps → HealthBuddy → Battery → **Unrestricted**
      (Xiaomi/Oppo/Realme: also enable **Autostart**)
- [x] **0.5** Note device + Android version in §15

Seed data after install: **Metformin 8:00 AM & 8:00 PM, Amlodipine 9:00 AM,
Sertraline 9:00 PM** — 4 doses today. Yesterday's Metformin 8:00 PM is pre-seeded
**Missed**, with no barrier: this is what makes the demo start immediately.

Optional shortcuts for demo prep:

```bash
adb shell pm grant org.dev.healthbuddy android.permission.POST_NOTIFICATIONS   # API 33+
adb shell appops set org.dev.healthbuddy SCHEDULE_EXACT_ALARM allow            # skip the settings trip
adb shell dumpsys alarm | grep -A 3 healthbuddy                                # see scheduled alarms
```

---

## 1. Onboarding — `ONB`

- [ ] **ONB-01** Cold start shows onboarding: name field, 2×2 language grid, **Reminders**
      card, disclaimer, **Get started**
- [ ] **ONB-02** Type a name → it is used in the Today title. Name is optional:
      leaving it blank still completes onboarding and shows a plain greeting
- [ ] **ONB-03** Tap each language (English / हिंदी / मराठी / ଓଡ଼ିଆ):
      title, body, rows and button switch immediately
- [ ] **ONB-04** Reminders card starts **on** and lists **Notifications** and
      **Alarms & reminders** with **Turn on** (or **On** if already granted)
- [ ] **ONB-05** Android 13+: tap **Turn on** under Notifications → system dialog →
      Allow → row flips to **On**
- [ ] **ONB-06** Tap **Turn on** under Alarms & reminders → system screen → allow →
      Back → row flips to **On**
- [ ] **ONB-07** With both On, **Get started** is enabled. Tap it → Today, greeted by name
- [ ] **ONB-08** Opt-out path (re-test after reset): switch **Reminders** off →
      rows hide, **Get started** enables, app works without notifications
- [ ] **ONB-09** Kill from recents → relaunch → no onboarding, data intact

---

## 2. Today — `TOD`

- [ ] **TOD-01** Greeting includes the name; today's date header is correct
- [ ] **TOD-02** Progress reads **"0 of 4 taken"** and the thin bar is empty
- [ ] **TOD-03** Log one dose → **"1 of 4 taken"**, bar fills ~25%. No score, no streak
- [ ] **TOD-04** **Needs attention** card is visible for yesterday's missed Metformin
- [ ] **TOD-05** Tapping it opens the dose sheet directly at
      *"What made it difficult today?"* (not at the Taken/Missed buttons)
- [ ] **TOD-06** Today's schedule rows show time, medicine, dose and status; tapping a
      row opens the log sheet
- [ ] **TOD-07** **Next appointment** card shows the follow-up visit with relative date
- [ ] **TOD-08** Health tip card is present and switches with language
- [ ] **TOD-09** Toggle all medicines inactive → schedule shows its empty state and the
      progress bar is hidden
- [ ] **TOD-10** Private mode on → medicine names hidden, times/status still readable

---

## 3. Dose flow — `FLW`

One sheet, four steps. Test each entry point.

- [ ] **FLW-01** Tap a dose → **Taken** → confirmation ("did you take it?" / Done) →
      **Close** → snackbar confirms
- [ ] **FLW-02** Tap a dose → **Missed** → barrier question with **10** one-tap options;
      selection gives a confirm haptic
- [ ] **FLW-03** Tap a dose → **Not sure** → reassurance screen, **Close**; no barrier asked
- [ ] **FLW-04** Dismiss the sheet mid-flow → nothing is corrupted; reopening a Missed
      dose with no barrier returns to the barrier question
- [ ] **FLW-05** After a support card, **Done** → *"Was this helpful?"* →
      Yes / Not sure / No → Close
- [ ] **FLW-06** The confirmation snackbar appears **after** the sheet closes, not under it
- [ ] **FLW-07** Re-log an already logged dose (History or Today): barrier + helpfulness
      reset, an existing note is preserved
- [ ] **FLW-08** TalkBack: dose rows and icon buttons announce a meaningful label

### 3.1 Every barrier — `BAR`

For each: log a dose → **Missed** → tap the barrier → check the support card + action.

| ID | Barrier | Expected action | Safety / notes |
|---|---|---|---|
| BAR-01 | It slipped my mind | **Adjust reminder** → +15 / +30 / +60 min chips | New time is used by Today/History and the alarm |
| BAR-02 | It costs too much | Care options: PHC · Jan Aushadhi · helpline **104** | Amber note: *"Never stop or change a prescribed medicine on your own."* |
| BAR-03 | The pharmacy had none | Care options + **104** | Note: do not take a different medicine or change the dose |
| BAR-04 | Side effects | Read checklist | Note: *"Do not stop the medicine on your own."* + when to seek help |
| BAR-05 | I get confused about doses | Read checklist | No advice beyond the reviewed copy |
| BAR-06 | I don't want anyone to see | **Turn on private mode** (not a reminder shift) | Names hide immediately + in-place confirmation |
| BAR-07 | Getting to the clinic is hard | Care options + **104** | No blame |
| BAR-08 | I feel low / distressed | **Call 14416** | System dialer opens with 14416; no call is placed |
| BAR-09 | Something else | Read checklist | Generic, safe |
| BAR-10 | I'd rather not say | No action | App must not nag, badge, or re-ask |

- [ ] **BAR-11** COST card contains no cost estimate, no dose change, no diagnosis
- [ ] **BAR-12** SIDE EFFECTS card never suggests stopping the medicine
- [ ] **BAR-13** DISTRESS dials only, never auto-calls; 14416 is visible in the copy
- [ ] **BAR-14** PREFER_NOT_TO_SAY: follow-up still works; no repeated prompts

---

## 4. Medicines — `MED`

- [ ] **MED-01** **+** FAB → sheet rises over the list (no new screen, no back arrow)
- [ ] **MED-02** New medicine has **no time selected**; **Save** disabled until name + time
- [ ] **MED-03** Preset chips toggle; labels are 12-hour (`8:00 AM`, `8:00 PM`)
- [ ] **MED-04** **Add time** → Material 3 clock dial **with AM/PM** → pick `8:15 PM` →
      **Done** → a removable chip appears
- [ ] **MED-05** Tap the **✕** on a custom chip → it is removed
- [ ] **MED-06** Save → row shows `8:00 AM · 8:15 PM` (dot = multiple times), plus dose
      and condition; times sorted
- [ ] **MED-07** Tap a row → same sheet, prefilled, title **Edit medicine**; saving updates
      instead of duplicating
- [ ] **MED-08** Active switch off → removed from Today and no reminder; on → returns
- [ ] **MED-09** Swipe a row left past the threshold → it is removed immediately
      (the red background previews the delete); no trash icon on the row.
      With TalkBack on, the same action is in the row's accessibility menu
- [ ] **MED-10** Delete every medicine → empty state with an add prompt
- [ ] **MED-11** Pick a time equal to a preset (e.g. 8:00 PM) → no duplicate chip

---

## 5. History — `HIS`

- [ ] **HIS-01** Only the last **30 days** appear (today − 29 d); nothing older
- [ ] **HIS-02** Entries grouped by day with **Today / Yesterday / d MMM**
- [ ] **HIS-03** Each entry shows status, barrier chip when present, helpfulness
- [ ] **HIS-04** A barrier with no answer shows **Yes / Not sure / No** chips; tapping one
      saves without opening the flow
- [ ] **HIS-05** **Weekly barrier counts** card appears only when a barrier exists;
      counts only — no advice, no score; hidden when empty
- [ ] **HIS-06** **Add a note** on any entry (≤140 chars) → shows on the entry;
      **Edit note** updates it
- [ ] **HIS-07** **Share summary** → Android share sheet → plain text containing 30-day
      counts, barriers, notes and medicines; no network, no permission prompt
- [ ] **HIS-08** Private mode on → note lines in the shared summary omit the medicine
      name (the medicines list itself is still included)
- [ ] **HIS-09** Empty state when nothing has been logged

---

## 6. Notifications — `NTF`

The core of this release. Use a medicine you can delete afterwards.

- [ ] **NTF-01** System Settings → Apps → HealthBuddy → Notifications → channel
      **Reminders** exists with the app language's name
- [ ] **NTF-02** Set a time exactly **2 minutes ahead**, background the app → it fires at
      that minute (allow ~10 s)
- [ ] **NTF-03** Full/expanded notification, one detail per line:

```
Metformin
8:00 AM
Dose: 500 mg
For: Type 2 diabetes

Open HealthBuddy to log this dose.
```

- [ ] **NTF-04** Collapsed state shows the name + time on one line (Android only allows
      one body line when collapsed — this is not a bug)
- [ ] **NTF-05** Private mode **off** → lock the phone → the **lock screen shows the
      details** (name, time, dose, condition)
- [ ] **NTF-06** Private mode **on** → new notification is generic
      (**Time for your medicine**), and the lock screen is generic too
- [ ] **NTF-07** Tap the notification → app opens on **Today**, even if Settings or a
      Learn detail was the last screen
- [ ] **NTF-08** Log the dose in the app **before** the alarm → no notification appears
- [ ] **NTF-09** Log it from the notification-triggered session **after** it fired →
      the notification is dismissed
- [ ] **NTF-10** Settings → Reminders off → pending alarms cancel and existing reminders
      disappear; on → alarms re-arm
- [ ] **NTF-11** Edit a medicine's time → the old alarm is gone, the new one fires
      (verify with `adb shell dumpsys alarm | grep -A 3 healthbuddy`)
- [ ] **NTF-12** Toggle a medicine inactive / delete it → no reminder
- [ ] **NTF-13** Reboot (`adb reboot`) → alarms are restored by the boot receiver
- [ ] **NTF-14** Revoke **Alarms & reminders** in system settings → app still schedules
      (inexact fallback, may be a few minutes late); no crash
- [ ] **NTF-15** Switch app language to हिंदी → next notification body/labels are Hindi
- [ ] **NTF-16** Two medicines at the same time → two separate notifications, no crash
- [ ] **NTF-17** Force-stop the app → no alarms until it is opened again
      (Android behaviour, documented limitation)

Failure triage:

```bash
adb shell dumpsys alarm | grep -A 3 healthbuddy     # is the alarm there?
adb shell dumpsys notification --noredact | grep -A 5 healthbuddy
adb logcat -d | grep -iE "healthbuddy|AlarmManager|Notification"
```

---

## 7. Settings — `SET`

- [ ] **SET-01** Language: all four switch instantly and persist across a restart
- [ ] **SET-02** Theme: System / Light / Dark — no restart, readable in both
- [ ] **SET-03** Reminders card: switch, On/Turn on states, **Open settings** escape after
      a denial, state re-reads when you come back
- [ ] **SET-04** Private mode: hides names on Today/History and in notifications
- [ ] **SET-05** **Replay demo scenario** → name Ravi, Ravi's data, **and** the missed dose
      opens at the barrier question
- [ ] **SET-06** **Reset all data** → confirm → onboarding returns, alarms are cancelled
- [ ] **SET-07** About text is present, no medical claims

---

## 8. Learn & Why — `LRN`

- [ ] **LRN-01** **Learn** shows six plain-language cards
- [ ] **LRN-02** Open a card → detail → back returns to the list
- [ ] **LRN-03** **Why this matters** (from Learn) shows the loop, the research question
      and the safety stance
- [ ] **LRN-04** Airplane mode → Learn, Why, logging and History all still work

---

## 9. Localization — `I18N`

- [ ] **I18N-01** Tabs, sheets, buttons, Settings, History labels all translate in
      EN / HI / MR / OR
- [ ] **I18N-02** Clinical copy (barriers, education) is EN + HI only; MR/OR fall back to
      English **by design** (PRD §11.3)
- [ ] **I18N-03** Odia text renders fully (no boxes/tofu)
- [ ] **I18N-04** Switching language mid-session updates the current screen immediately
- [ ] **I18N-05** Notification content uses the app language at fire time

---

## 10. Persistence & lifecycle — `DATA`

- [ ] **DATA-01** Kill from recents → relaunch → name, language, medicines, logs,
      appointments, notes, theme, private mode, reminder toggle all preserved
- [ ] **DATA-02** Rotate on every tab and with the add/edit sheet open → state survives
- [ ] **DATA-03** Font scale 1.3 → no clipped buttons or text
- [ ] **DATA-04** Airplane mode → no errors, no spinners, no network calls
- [ ] **DATA-05** Deny both permissions → no crash; the gate explains what to do

---

## 11. Safety — `SAFE`

- [ ] **SAFE-01** The app never gives medical advice outside the reviewed cards
- [ ] **SAFE-02** COST and SIDE EFFECTS both show their safety notes
- [ ] **SAFE-03** DISTRESS offers **14416** and uses the system dialer only
- [ ] **SAFE-04** Facility cards offer **104** and labelled care options
- [ ] **SAFE-05** "I'd rather not say" is never re-asked, badged or shamed
- [ ] **SAFE-06** STIGMA turns on private mode instead of nagging
- [ ] **SAFE-07** Private-mode notifications never name the medicine (lock screen too)
- [ ] **SAFE-08** No score, streak, leaderboard, or "failure" language anywhere
- [ ] **SAFE-09** No diagnosis or medicine recommendation is generated

---

## 12. Accessibility & UI — `A11Y`

- [ ] **A11Y-01** TalkBack announces icon buttons (FAB, remove, share, close)
- [ ] **A11Y-02** Haptics on barrier selection and save
- [ ] **A11Y-03** Touch targets feel ≥48 dp; nothing tiny next to the edge
- [ ] **A11Y-04** Light and dark both pass a quick contrast eyeball test
- [ ] **A11Y-05** Edge-to-edge: no content under the status/navigation bars
- [ ] **A11Y-06** Predictive back / screen transitions work; back never exits unexpectedly
- [ ] **A11Y-07** Every in-app icon is Lucide; no mixed icon sets
- [ ] **A11Y-08** TalkBack can complete the whole dose flow without getting stuck

---

## 13. Screen recording for the client

I can't record from this machine — use the phone's built-in screen recorder with the mic on
for narration, or `adb` for a silent capture:

```bash
adb shell screenrecord --size 1080x2400 --bit-rate 8M /sdcard/healthbuddy-demo.mp4
# press Ctrl+C to stop
adb pull /sdcard/healthbuddy-demo.mp4 ./healthbuddy-demo.mp4
```

`screenrecord` has no microphone audio; the built-in recorder is better if you want voice.

### Pre-flight (5 minutes before recording)

- [ ] Permissions already granted; **Private mode OFF**
- [ ] Settings → **Replay demo scenario** (deterministic Ravi data, opens the barrier step)
- [ ] Add a throwaway medicine/time **3 minutes ahead** for the notification moment
- [ ] DND off, media volume up, light mode, 12-hour clock
- [ ] Close personal notifications; use a demo device/account if possible
- [ ] Do **not** force-stop the app (that cancels alarms)

### Shot list (about 90 seconds)

| Time | Shot | Say / show |
|---|---|---|
| 0:00 | Today | "Every reminder app tells you when. HealthBuddy asks why." Show **0 of 4 taken** and the needs-attention card |
| 0:10 | Attention → barrier | Tap yesterday's missed dose → 10 barriers → **It costs too much** |
| 0:20 | Support card | Scroll: care options, **104**, the safety line. "It never tells you to stop a medicine." |
| 0:30 | Follow-up | **Done** → **Yes** → Close → snackbar |
| 0:38 | History | Entry with barrier + helpful; point at weekly counts; add a note |
| 0:48 | Share | **Share summary** → show the plain-text preview, cancel |
| 0:55 | Medicines | **+** → pick a time in the clock dialog (AM/PM visible) → Save |
| 1:05 | Notification | Pull down the shade → expand: name, time, dose, condition → tap → lands on Today |
| 1:15 | Progress | Log a today dose as Taken → **1 of 4** |
| 1:20 | Private mode | Settings → on → Today names hidden → off |
| 1:28 | Why this matters | Loop diagram for 2 seconds; end on the tagline |

If the notification timing slips, delay the start of the recording — do not skip it; it is
the feature the client is paying attention to.

Name the file `healthbuddy-demo-YYYYMMDD.mp4` and send it with
`healthBuddy/docs/one-slide.svg` and `healthBuddy/README.md`.

---

## 14. Known limitations (not bugs)

1. **Collapsed notifications are one line.** Android only allows a single body line when
   collapsed; the detail lines appear when expanded.
2. **MR/OR clinical copy is English** — unreviewed machine-translated medical text is worse
   than English (PRD §11.3). UI chrome is fully translated.
3. **Time zones are ignored** — demo scope; alarms use the phone's local time.
4. **No push/FCM, no backend, no login** — local only by design.
5. **Force-stop cancels alarms** until the app is opened again.
6. **OEM battery managers** (Xiaomi/Oppo/Realme/Vivo) may delay or kill alarms unless
   Autostart is enabled and battery is unrestricted.
7. **The onboarding gate has an opt-out** — switching reminders off is treated as an
   explicit "no reminders" and unlocks Get started.
8. **No automated tests** — this checklist is the test suite.

---

## 15. Sign-off

| Field | Value |
|---|---|
| Build / commit | |
| Device | |
| Android version | |
| Tester | |
| Date | |
| Result | ☐ Pass ☐ Pass with notes ☐ Fail |
| Failed IDs | |

### Failure report template

```
ID:            NTF-04
Build:         4a753d6 (+ local changes)
Device:        Pixel 7, Android 16
Steps:         1) ... 2) ...
Expected:      ...
Actual:        ...
Evidence:      screenshot / logcat / video timestamp
```

Capture logs with:

```bash
adb logcat -d > healthbuddy-logcat.txt
```
