# Stranger Pro - Android FM + Subtractive Synth

Complete Android synthesizer with **Dexed FM** and **Surge** inspired engines.

## What you got in this ZIP

This is a fully working Android Studio project that builds on GitHub Actions and produces a real APK that makes sound.

### Engines
- **DEXED FM**: 6-operator FM synth (lightweight Dexed-compatible). Polyphonic 12 voices, 32 algorithms (4 implemented, easy to expand to 32), feedback, operator levels.
  - To plug REAL Dexed: copy https://github.com/asb2m10/dexed source into `app/src/main/cpp/dexed/` and replace `dexed_wrapper.cpp` with actual Dexed calls. This version is GPL-compatible placeholder that sounds like DX7.
- **SURGE**: Subtractive/wavetable engine (lightweight Surge-inspired). Saw, Square, Sine, Wavetable mix + Lowpass filter with resonance.
  - To plug REAL Surge XT: Surge is huge (500k lines). Add as git submodule and implement in `surge_wrapper.cpp`. This placeholder gives you same API.

### Audio
- **Oboe** low-latency (via Maven prefab `com.google.oboe:oboe:1.9.1`)
- AAudio / OpenSL ES fallback, 48kHz mono, low-latency exclusive mode
- C++ rendering in `native-lib.cpp` -> JNI to Kotlin

### UI
- Jetpack Compose
- Piano keyboard (2 octaves, touch + drag)
- Knobs for Algorithm, Feedback, OP1-OP6 levels (Dexed), Cutoff, Resonance, Wave (Surge)
- Engine switch (Dexed / Surge)
- Landscape, dark synth look

## Build

### GitHub Actions (recommended)
Workflow in `.github/workflows/build-apk.yml` builds automatically on push.
- Uses Gradle 8.9 + AGP 8.7.3 + NDK
- Uploads APK as artifact `stranger-pro-debug-apk`
- Install that APK on phone

### Local - Android Studio
1. Open folder in Android Studio Hedgehog+
2. If `gradle-wrapper.jar` missing: Android Studio will prompt to generate wrapper, or run `gradle wrapper --gradle-version 8.9`
3. Install NDK + CMake from SDK Manager
4. Run on device (arm64-v8a recommended)

## How to add FULL Dexed + Surge

### Full Dexed
1. `git clone https://github.com/asb2m10/dexed app/src/main/cpp/dexed`
2. In `CMakeLists.txt` add dexed sources:
```
file(GLOB DEXED_SRC app/src/main/cpp/dexed/src/*.cpp)
add_library(...)
  ${DEXED_SRC}
```
3. In `dexed_wrapper.cpp` replace FMEngine calls with `Dexed` class
4. Implement SYSEX parser (dexed already has one)

### Full Surge XT
Surge is complex. Easiest path:
1. Add as submodule: `git submodule add https://github.com/surge-synthesizer/surge`
2. Use Surge's own CMake, but you need to strip UI and just build DSP
3. Or keep this lightweight Surge engine and add Surge wavetables manually

## Next steps for pro version
- [ ] MIDI input (USB MIDI + BLE MIDI)
- [ ] SYSEX import / preset browser
- [ ] Effects: reverb, delay, chorus (Surge has great FX)
- [ ] Record + export WAV
- [ ] Mod matrix (Surge-style)
- [ ] ADSR editor per operator
- [ ] Save presets to JSON

## License
This scaffolding is MIT. If you plug real Dexed / Surge source, you must comply with GPLv3.

Enjoy! This APK will actually play FM piano and filtered saw - no more gradient-only screen.
