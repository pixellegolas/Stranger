#pragma once
#include <cmath>
#include <array>
#include <vector>
#include <mutex>

#ifndef M_PI
#define M_PI 3.14159265358979323846
#endif

struct FMOperator {
    float phase = 0.0f;
    float env = 0.0f;
    enum Stage { OFF, ATTACK, DECAY, SUSTAIN, RELEASE } stage = OFF;
    float attackRate = 0.001f;
    float decayRate = 0.0005f;
    float sustainLevel = 0.7f;
    float releaseRate = 0.0008f;
    float ratio = 1.0f;
    float level = 1.0f;
    float lastOut = 0.0f;
    float feedback = 0.0f;

    void setFrequency(float baseFreq, float sampleRate);
    void noteOn(float velocity);
    void noteOff();
    float render(float modInput);
    bool isActive() const { return stage != OFF; }
};

class FMVoice {
public:
    static const int NUM_OPS = 6;
    std::array<FMOperator, NUM_OPS> ops;
    float baseFreq = 440.0f;
    int note = -1;
    bool active = false;
    int algorithm = 5; // 0-31
    float feedback = 0.5f;
    float velocity = 1.0f;
    int age = 0;

    void init(float sampleRate);
    void noteOn(int midiNote, float vel, float sampleRate);
    void noteOff();
    float render();
    bool isActive() const;
};

class FMEngine {
public:
    static const int MAX_VOICES = 12;
    std::array<FMVoice, MAX_VOICES> voices;
    float sampleRate = 48000.0f;
    int algorithm = 5;
    float feedback = 0.6f;
    float masterLevel = 0.4f;
    std::array<float, 6> opLevels = {1.0f, 1.0f, 1.0f, 1.0f, 1.0f, 0.8f};
    std::mutex mutex;

    void init(float sr);
    void noteOn(int note, float velocity);
    void noteOff(int note);
    void render(float* out, int numFrames);
    void setAlgorithm(int algo);
    void setFeedback(float fb);
    void setOpLevel(int op, float level);
    void setMasterLevel(float lvl);
};
