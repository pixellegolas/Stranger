#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "STRANGER", __VA_ARGS__)

class StrangerEngine : public oboe::AudioStreamCallback {
public:
    oboe::ManagedStream stream;
    float bassMode = 0; // 0=808, 1=FM, 2=OBXd
    float masterFilter = 0.5f;
    float drive = 0.2f;

    // Placeholder for real Dexed/Surge/OBXd instances
    // Dexed::Engine dexedEngine;
    // Surge::SurgeSynthesizer surgeSynth;
    // Obxd::Filter obxdFilter;

    bool init() {
        oboe::AudioStreamBuilder builder;
        builder.setDirection(oboe::Direction::Output)
               ->setPerformanceMode(oboe::PerformanceMode::LowLatency)
               ->setSharingMode(oboe::SharingMode::Exclusive)
               ->setFormat(oboe::AudioFormat::Float)
               ->setChannelCount(2)
               ->setSampleRate(48000)
               ->setCallback(this);
        auto res = builder.openManagedStream(stream);
        if(res != oboe::Result::OK) { LOGD("Oboe open failed %d", (int)res); return false; }
        stream->requestStart();
        LOGD("Engine started - Real Dexed+Surge+OBXd will replace this stub");
        return true;
    }

    // Ladder filter approx for OB-Xd bass
    float ladderState[4] = {0};
    float ladderFilter(float in, float cutoff, float res) {
        float f = cutoff * 1.16f;
        float fb = res * (1.0f - 0.15f * f * f);
        for(int i=0;i<4;i++){
            ladderState[i] = ladderState[i] + f * (in - ladderState[i] - fb * ladderState[3]);
            in = ladderState[i];
        }
        return ladderState[3];
    }

    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *s, void *audioData, int32_t numFrames) override {
        float *out = static_cast<float*>(audioData);
        // TODO: Replace with real Dexed/Surge/OBXd rendering
        // For now generate 808 SUB as placeholder that sounds better than web FM
        static double phase = 0;
        for(int i=0;i<numFrames;i++){
            float sample = 0;
            // Simple 808 sub placeholder - real engine will be Dexed FM bass + OB-Xd
            if(bassMode==0){ // 808
                double freq = 55.0 * exp(-0.0008 * (phase));
                sample = sin(phase) * 0.6f * exp(-0.0005f * fmod(phase, 10000));
                phase += 2*M_PI*freq/48000.0;
            }
            // Write stereo
            out[i*2] = sample * (1.0f + drive);
            out[i*2+1] = sample * (1.0f + drive);
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static StrangerEngine gEngine;

extern "C" JNIEXPORT jboolean JNICALL
Java_com_strangerpro_MainActivity_initAudioEngine(JNIEnv*, jobject){ return gEngine.init(); }

extern "C" JNIEXPORT void JNICALL
Java_com_strangerpro_MainActivity_noteOn(JNIEnv*, jobject, jint note, jfloat vel, jint engine){
    LOGD("noteOn %d %.2f engine %d", note, vel, engine);
    // gEngine.dexed.noteOn(note, vel)
    // gEngine.surge.playNote
}

extern "C" JNIEXPORT void JNICALL
Java_com_strangerpro_MainActivity_noteOff(JNIEnv*, jobject, jint note){}

extern "C" JNIEXPORT void JNICALL
Java_com_strangerpro_MainActivity_setParam(JNIEnv*, jobject, jint id, jfloat v){
    if(id==100) gEngine.drive = v;
    if(id==101) gEngine.masterFilter = v;
}

extern "C" JNIEXPORT void JNICALL
Java_com_strangerpro_MainActivity_setBassMode(JNIEnv*, jobject, jint mode){ gEngine.bassMode = mode; }

extern "C" JNIEXPORT jboolean JNICALL
Java_com_strangerpro_MainActivity_loadSampleFd(JNIEnv*, jobject, jint fd){ LOGD("loadSample fd %d", fd); return true; }
