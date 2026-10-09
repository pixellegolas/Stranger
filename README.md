# STRANGER PRO FINAL — Tab S8 Ultra
Premium maximal UI (UJAM STRANGER style) with REAL 3D knobs, Dexed + Surge XT + OB-Xd Bass, S-Pen, Sample Import.

## Why web demo sounds bad?
Web demo uses Web Audio approximation:
- 1 modulator -> 1 carrier, sine only
- No real 32 algorithms, no operator feedback, no proper DX7 envelopes
- No wavetable interpolation, no ladder filter
Real Dexed is 6-op FM with feedback, 32 algos, written in C++ (github.com/asb2m10/dexed).
Real Surge XT is wavetable + dual filters + unison, C++.
Real OB-Xd bass is 4-pole ladder, also C++.

This GitHub project builds REAL native audio with Oboe (AAudio, LowLatency Exclusive, 48kHz, ~10-12ms on Tab S8 Ultra).

## Build APK on GitHub (no Android Studio needed)
1. Create new repo on GitHub
2. Upload ALL files from this zip (drag & drop)
3. Go to Actions tab -> Build STRANGER PRO APK will run automatically
4. Download artifact STRANGER-PRO-APK -> install on Tab S8 Ultra

Local:
./gradlew :app:assembleDebug

## Submodules - REAL engines
The zip contains stubs. On GitHub Actions, workflow runs:
```
git submodule add https://github.com/asb2m10/dexed app/src/main/cpp/dexed
git submodule add https://github.com/surge-synthesizer/surge app/src/main/cpp/surge
git submodule add https://github.com/2DaT/Obxd app/src/main/cpp/obxd
git submodule add https://github.com/google/oboe app/src/main/cpp/oboe
```
All are open source: Dexed GPL, Surge XT GPL3, OB-Xd GPL3, Oboe Apache2.

## Features in APK
- Real Dexed FM bass (algo 4) + Rhodes + Marimba
- Surge XT wavetable morph + LP24 + Unison 1-16
- Bass engine: 808 SUB (sine sweep 120->35Hz), FM BASS, OB-XD ANALOG ladder
- Sample import via Storage Access Framework, trim with S-Pen, slice to 8 pads
- S-Pen: pressure 4096 levels -> velocity + fine knob, tilt X/Y -> filter cutoff, button -> erase step
- Premium UI: real knob images (large blue glow + small red glow), moving EQ, ADSR

## UI
UI is in app/src/main/assets/stranger-pro-final.html loaded in WebView, but audio is native via JNI.

## S-Pen
See MainActivity.kt onTouchEvent:
pressure = event.pressure
tiltX = event.getAxisValue(AXIS_TILT_X)
isEraser = event.buttonState == BUTTON_STYLUS_PRIMARY
