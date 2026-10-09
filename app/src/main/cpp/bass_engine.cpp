// 808 SUB, FM BASS Dexed algo 4, OB-XD ladder
#include <cmath>
struct Bass808 { float phase=0,env=1; float process(float f){ phase+=f/48000*6.283f; env*=0.9995f; return sinf(phase)*env*0.8f; } };
