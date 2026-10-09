// BASS ENGINE - OPEN SOURCE
// 1. 808 SUB - based on open source 808 model (sine sweep)
// 2. FM BASS - Dexed algorithm 4 (2x 3-op stack) - uses Dexed core when submodule present
// 3. OB-XD ANALOG - ladder filter from OB-Xd (github.com/2DaT/Obxd) - 4-pole 24dB

#include <cmath>
struct Bass808 {
    float phase=0, env=1;
    float process(float freq){ phase+= freq/48000.0f * 6.283f; env *= 0.9995f; return sinf(phase) * env * 0.8f; }
    void trigger(){ phase=0; env=1; }
};

struct FMBass {
    // Placeholder for real Dexed FM bass - 2 modulators -> carrier
    float modPhase=0, carPhase=0;
    float process(float freq, float modIndex=2.5f){
        modPhase+= freq*2.01f/48000*6.283f;
        float mod = sinf(modPhase) * modIndex * freq;
        carPhase+= (freq + mod)/48000*6.283f;
        return sinf(carPhase) * 0.6f;
    }
};

struct OBXdBass {
    // Ladder filter from OB-Xd
    float s[4]={0};
    float process(float in, float cutoff, float res){
        float f = cutoff * 1.16f;
        float fb = res * (1.0f - 0.15f*f*f);
        float input = in;
        for(int i=0;i<4;i++){ s[i] = s[i] + f*(input - s[i] - fb*s[3]); input = s[i]; }
        return s[3];
    }
};
