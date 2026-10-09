// BASS ENGINE - OPEN SOURCE
// 808 SUB, FM BASS (Dexed algo 4), OB-XD ANALOG ladder
#include <cmath>
struct Bass808 { float phase=0, env=1; float process(float freq){ phase+= freq/48000.0f * 6.283f; env *= 0.9995f; return sinf(phase) * env * 0.8f; } void trigger(){ phase=0; env=1; } };
struct FMBass { float modPhase=0, carPhase=0; float process(float freq, float modIndex=2.5f){ modPhase+= freq*2.01f/48000*6.283f; float mod = sinf(modPhase) * modIndex * freq; carPhase+= (freq + mod)/48000*6.283f; return sinf(carPhase) * 0.6f; } };
struct OBXdBass { float s[4]={0}; float process(float in, float cutoff, float res){ float f = cutoff * 1.16f; float fb = res * (1.0f - 0.15f*f*f); float input = in; for(int i=0;i<4;i++){ s[i] = s[i] + f*(input - s[i] - fb*s[3]); input = s[i]; } return s[3]; } };
