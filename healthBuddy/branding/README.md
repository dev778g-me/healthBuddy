# Branding sources

Not shipped. Everything the app packages lives under `app/src/main/res/`.

| File | Purpose |
|---|---|
| `healthbuddy-icons-v3/res/drawable/ic_launcher_foreground.xml` | adaptive-icon foreground: cream heart + capsule, soft shadow |
| `healthbuddy-icons-v3/res/drawable/ic_launcher_background.xml` | adaptive-icon background gradient (`#34977D` → `#1F6F5C`) |
| `healthbuddy-icons-v3/res/drawable/ic_launcher_monochrome.xml` | Android 13+ themed-icon layer (capsule cut out of the heart) |
| `play_store_icon.png` | 512 px listing art, not part of the APK |

## How the launcher icon is wired

- `res/mipmap-anydpi/ic_launcher.xml` **and** `ic_launcher_round.xml` are `<adaptive-icon>`
  documents pointing at `@drawable/ic_launcher_{foreground,background,monochrome}`.
- `minSdk 31`, so Android always resolves `mipmap-anydpi`; there are deliberately **no**
  density-specific `mipmap-*dpi` bitmaps.
- The foreground canvas is 108 × 108 dp and the guaranteed-visible safe zone is a
  66 dp circle centred on it — keep all art inside that circle or launcher masks clip it.
- The system splash screen reuses the launcher icon; no separate splash asset exists.

## Changing the icon

1. Edit the three vectors here, keeping the 108 dp viewport and the safe zone.
2. Copy them over the copies in `app/src/main/res/drawable/`.
3. Reinstall (not just install over) so the launcher drops its cached bitmap.
