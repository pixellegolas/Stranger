#pragma once
#include <cmath>
#include <array>
#include <mutex>
#include <vector>

// Surge XT inspired subtractive/wavetable engine - lightweight version
// Real Surge is huge (https://github.com/surge-synthesizer/surge)
// This wrapper provides a working subtractive engine with same API surface
// so you can swap in real Surge later

struct SurgeVoice {
    float phase = 0.0f;
    float filterState = 0.0f;
    int note = -1;
    bool active = false;
    float env = 0.0f;
    enum Stage { OFF, ATTACK, DECAY, SUSTAIN, RELEASE } stage = OFF;
    float freq = 440.0f;
    float velocity = 1.0f;
    float cutoff = 0.5f; // 0-1
    float resonance = 0.2f;
    int waveType = 0; // 0 saw, 1 square, 2 sine, 3 wavetable
    int age = 0;

    void noteOn(int midiNote, float vel, float sampleRate);
    void noteOff();
    float render(float sampleRate);
    bool isActive() const { return stage != OFF; }
};

class SurgeWrapper {
public:
    static const int MAX_VOICES = 12;
    std::array<SurgeVoice, MAX_VOICES> voices;
    float sampleRate = 48000.0f;
    float cutoff = 0.6f;
    float resonance = 0.3f;
    float masterLevel = 0.4f;
    int waveType = 0;
    std::mutex mutex;

    void init(float sr);
    void noteOn(int note, int velocity);
    void noteOff(int note);
    void render(float* out, int numFrames);
    void setCutoff(float c);
    void setResonance(float r);
    void setWaveType(int t);
};
