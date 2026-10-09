#pragma once
#include "fm_engine.h"

// Dexed-compatible wrapper - mimics Dexed API but uses our FMEngine
// To plug real Dexed: replace this implementation with actual Dexed engine
// https://github.com/asb2m10/dexed

class DexedWrapper {
public:
    FMEngine engine;
    void init(float sampleRate) { engine.init(sampleRate); }
    void noteOn(int note, int velocity) { engine.noteOn(note, velocity/127.0f); }
    void noteOff(int note) { engine.noteOff(note); }
    void render(float* out, int frames) { engine.render(out, frames); }
    void setAlgorithm(int a) { engine.setAlgorithm(a); }
    void setFeedback(float fb) { engine.setFeedback(fb); }
    void setOpLevel(int op, float lvl) { engine.setOpLevel(op, lvl); }
    // Dexed-specific: sysex handling placeholder
    bool loadSysex(const char* data, int size) { return true; } // TODO: implement SYSEX parser
};
