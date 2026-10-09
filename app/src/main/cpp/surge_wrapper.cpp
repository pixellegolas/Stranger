#include "surge_wrapper.h"
#include <cmath>

static float midiToFreq(int note) {
    return 440.0f * powf(2.0f, (note - 69) / 12.0f);
}

void SurgeVoice::noteOn(int midiNote, float vel, float sampleRate) {
    note = midiNote;
    freq = midiToFreq(midiNote);
    velocity = vel;
    active = true;
    stage = ATTACK;
    env = 0.0f;
    phase = 0.0f;
    filterState = 0.0f;
    age = 0;
}

void SurgeVoice::noteOff() {
    if (stage != OFF) stage = RELEASE;
}

float SurgeVoice::render(float sampleRate) {
    if (stage == OFF) { active=false; return 0.0f; }
    age++;
    // Envelope
    switch(stage) {
        case ATTACK: env += 0.01f; if (env>=1.0f){env=1.0f; stage=DECAY;} break;
        case DECAY: env -= 0.002f; if (env<=0.7f){env=0.7f; stage=SUSTAIN;} break;
        case SUSTAIN: break;
        case RELEASE: env -= 0.004f; if (env<=0.0f){env=0.0f; stage=OFF; active=false; return 0.0f;} break;
        default: break;
    }

    // Oscillator
    phase += freq / sampleRate;
    if (phase >= 1.0f) phase -= 1.0f;
    float osc = 0.0f;
    switch(waveType) {
        case 0: // Saw
            osc = 2.0f*phase - 1.0f;
            break;
        case 1: // Square
            osc = phase < 0.5f ? 1.0f : -1.0f;
            break;
        case 2: // Sine
            osc = sinf(2.0f*M_PI*phase);
            break;
        case 3: // Wavetable-ish (saw + sine mix)
            osc = (2.0f*phase-1.0f)*0.5f + sinf(2.0f*M_PI*phase*2.0f)*0.5f;
            break;
        default:
            osc = 2.0f*phase-1.0f;
    }

    // Simple 1-pole lowpass for Surge-like filter
    // cutoff 0-1 maps to 0.01 - 0.4
    float fc = 0.01f + cutoff * 0.4f;
    // resonance boost
    float res = 1.0f + resonance * 4.0f;
    filterState += fc * (osc * res - filterState);
    float filtered = filterState;

    return filtered * env * velocity;
}

void SurgeWrapper::init(float sr) {
    sampleRate = sr;
}

void SurgeWrapper::noteOn(int note, int velocity) {
    std::lock_guard<std::mutex> lock(mutex);
    SurgeVoice* free = nullptr;
    SurgeVoice* oldest = &voices[0];
    for (auto &v: voices) {
        if (!v.isActive()) { free=&v; break; }
        if (v.age > oldest->age) oldest=&v;
    }
    if (!free) free=oldest;
    free->cutoff = cutoff;
    free->resonance = resonance;
    free->waveType = waveType;
    free->noteOn(note, velocity/127.0f, sampleRate);
}

void SurgeWrapper::noteOff(int note) {
    std::lock_guard<std::mutex> lock(mutex);
    for (auto &v: voices) if (v.note==note && v.isActive()) v.noteOff();
}

void SurgeWrapper::render(float* out, int numFrames) {
    std::lock_guard<std::mutex> lock(mutex);
    for (int i=0;i<numFrames;i++) {
        float mix=0;
        for (auto &v: voices) if (v.isActive()) mix+=v.render(sampleRate);
        mix *= masterLevel;
        if (mix>1.0f) mix=1.0f;
        if (mix<-1.0f) mix=-1.0f;
        out[i]=mix;
    }
}

void SurgeWrapper::setCutoff(float c) { std::lock_guard<std::mutex> lock(mutex); cutoff=c; for(auto&v:voices) v.cutoff=c; }
void SurgeWrapper::setResonance(float r) { std::lock_guard<std::mutex> lock(mutex); resonance=r; for(auto&v:voices) v.resonance=r; }
void SurgeWrapper::setWaveType(int t) { std::lock_guard<std::mutex> lock(mutex); waveType=t; for(auto&v:voices) v.waveType=t; }
