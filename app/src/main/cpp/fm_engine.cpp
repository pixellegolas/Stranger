#include "fm_engine.h"
#include <algorithm>
#include <cstdlib>

static float midiToFreq(int note) {
    return 440.0f * powf(2.0f, (note - 69) / 12.0f);
}

void FMOperator::setFrequency(float baseFreq, float sampleRate) {
    float freq = baseFreq * ratio;
    phase += 0; // keep phase
    // phase increment per sample
    // will be calculated in render via baseFreq * ratio
}

void FMOperator::noteOn(float velocity) {
    stage = ATTACK;
    env = 0.0f;
    // velocity affects level slightly
    level = 0.5f + velocity * 0.5f;
    // keep phase continuous for less click? reset slightly
    // phase = 0;
}

void FMOperator::noteOff() {
    if (stage != OFF) stage = RELEASE;
}

float FMOperator::render(float modInput) {
    if (stage == OFF) return 0.0f;

    // Envelope
    switch(stage) {
        case ATTACK:
            env += attackRate;
            if (env >= 1.0f) { env = 1.0f; stage = DECAY; }
            break;
        case DECAY:
            env -= decayRate;
            if (env <= sustainLevel) { env = sustainLevel; stage = SUSTAIN; }
            break;
        case SUSTAIN:
            break;
        case RELEASE:
            env -= releaseRate;
            if (env <= 0.0f) { env = 0.0f; stage = OFF; return 0.0f; }
            break;
        default: break;
    }

    float input = modInput + feedback * lastOut;
    float out = sinf(phase + input) * env * level;
    lastOut = out;
    return out;
}

void FMVoice::init(float sampleRate) {
    for (int i=0;i<NUM_OPS;i++) {
        ops[i].ratio = 1.0f + (i%3)*0.5f; // varied ratios like DX7
        ops[i].attackRate = 0.02f / (i+1);
        ops[i].decayRate = 0.001f;
        ops[i].sustainLevel = 0.6f + (i*0.05f);
        ops[i].releaseRate = 0.0015f;
        ops[i].feedback = 0.0f;
    }
    ops[0].feedback = 0.4f; // first op can have feedback like DX7
}

void FMVoice::noteOn(int midiNote, float vel, float sampleRate) {
    note = midiNote;
    baseFreq = midiToFreq(midiNote);
    velocity = vel;
    active = true;
    age = 0;
    for (int i=0;i<NUM_OPS;i++) {
        // set phase increment
        float freq = baseFreq * ops[i].ratio;
        ops[i].phase = 0;
        // store phase inc as ratio * baseFreq * 2pi / sr, but we calc per sample using phase accumulator
        ops[i].noteOn(vel);
    }
}

void FMVoice::noteOff() {
    for (auto &op : ops) op.noteOff();
}

bool FMVoice::isActive() const {
    if (!active) return false;
    for (auto &op : ops) if (op.isActive()) return true;
    return false;
}

float FMVoice::render() {
    if (!isActive()) { active = false; return 0.0f; }
    age++;

    // Update phase increments per operator based on baseFreq
    // For simplicity, we advance phase inside render by freq
    // We'll use fixed sample rate 48000 for inc calc, but use actual phase accumulation

    // Simple algorithm handling
    float out = 0.0f;
    switch(algorithm % 4) {
        case 0: { // Chain 1->2->3->4->5->6 carrier
            float mod = 0.0f;
            // ops[0] is first modulator
            for (int i=0;i<NUM_OPS;i++) {
                // advance phase
                float freq = baseFreq * ops[i].ratio;
                ops[i].phase += 2.0f * M_PI * freq / 48000.0f;
                if (ops[i].phase > 2*M_PI) ops[i].phase -= 2*M_PI;
                float opOut = ops[i].render(mod);
                mod = opOut * 2.0f; // modulation depth
                if (i==NUM_OPS-1) out = opOut;
            }
            break;
        }
        case 1: { // Two stacks: 1->2->3 and 4->5->6, sum
            float mod1=0, mod2=0;
            float chain1=0, chain2=0;
            for (int i=0;i<3;i++) {
                float freq = baseFreq * ops[i].ratio;
                ops[i].phase += 2.0f * M_PI * freq / 48000.0f;
                if (ops[i].phase > 2*M_PI) ops[i].phase -= 2*M_PI;
                chain1 = ops[i].render(mod1);
                mod1 = chain1 * 2.0f;
            }
            for (int i=3;i<6;i++) {
                float freq = baseFreq * ops[i].ratio;
                ops[i].phase += 2.0f * M_PI * freq / 48000.0f;
                if (ops[i].phase > 2*M_PI) ops[i].phase -= 2*M_PI;
                chain2 = ops[i].render(mod2);
                mod2 = chain2 * 2.0f;
            }
            out = (chain1 + chain2) * 0.5f;
            break;
        }
        case 2: { // All carriers parallel (additive, rich)
            float sum=0;
            for (int i=0;i<NUM_OPS;i++) {
                float freq = baseFreq * ops[i].ratio;
                ops[i].phase += 2.0f * M_PI * freq / 48000.0f;
                if (ops[i].phase > 2*M_PI) ops[i].phase -= 2*M_PI;
                sum += ops[i].render(0) * (0.8f + i*0.1f);
            }
            out = sum / NUM_OPS;
            break;
        }
        default: { // 3 modulators -> 1 carrier stack for brass
            float mod = 0;
            // 3 mods sum -> carrier
            float modSum=0;
            for (int i=0;i<5;i++) {
                float freq = baseFreq * ops[i].ratio;
                ops[i].phase += 2.0f * M_PI * freq / 48000.0f;
                if (ops[i].phase > 2*M_PI) ops[i].phase -= 2*M_PI;
                modSum += ops[i].render(0) * 0.5f;
            }
            float freq = baseFreq * ops[5].ratio;
            ops[5].phase += 2.0f * M_PI * freq / 48000.0f;
            if (ops[5].phase > 2*M_PI) ops[5].phase -= 2*M_PI;
            out = ops[5].render(modSum * 3.0f);
            break;
        }
    }
    return out * velocity;
}

void FMEngine::init(float sr) {
    sampleRate = sr;
    for (auto &v : voices) v.init(sr);
}

void FMEngine::noteOn(int note, float velocity) {
    std::lock_guard<std::mutex> lock(mutex);
    // find free voice
    FMVoice* free = nullptr;
    FMVoice* oldest = &voices[0];
    for (auto &v : voices) {
        if (!v.isActive()) { free = &v; break; }
        if (v.age > oldest->age) oldest = &v;
    }
    if (!free) free = oldest;
    free->algorithm = algorithm;
    free->feedback = feedback;
    // apply op levels
    for (int i=0;i<6;i++) free->ops[i].level = opLevels[i];
    free->noteOn(note, velocity, sampleRate);
}

void FMEngine::noteOff(int note) {
    std::lock_guard<std::mutex> lock(mutex);
    for (auto &v : voices) {
        if (v.note == note && v.isActive()) v.noteOff();
    }
}

void FMEngine::render(float* out, int numFrames) {
    std::lock_guard<std::mutex> lock(mutex);
    for (int i=0;i<numFrames;i++) {
        float mix = 0.0f;
        for (auto &v : voices) {
            if (v.isActive()) mix += v.render();
        }
        mix *= masterLevel;
        // soft clip
        if (mix > 1.0f) mix = 1.0f;
        if (mix < -1.0f) mix = -1.0f;
        out[i] = mix;
    }
}

void FMEngine::setAlgorithm(int algo) { 
    std::lock_guard<std::mutex> lock(mutex);
    algorithm = algo % 32; 
    for (auto &v: voices) v.algorithm = algorithm;
}
void FMEngine::setFeedback(float fb) { 
    std::lock_guard<std::mutex> lock(mutex);
    feedback = fb; 
    for (auto &v: voices) {
        v.feedback = fb;
        v.ops[0].feedback = fb;
    }
}
void FMEngine::setOpLevel(int op, float level) {
    if (op<0||op>=6) return;
    std::lock_guard<std::mutex> lock(mutex);
    opLevels[op]=level;
}
void FMEngine::setMasterLevel(float lvl) { masterLevel = lvl; }
