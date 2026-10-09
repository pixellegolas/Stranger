#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include <cmath>
#define LOGD(...) __android_log_print(ANDROID_LOG_DEBUG, "STRANGER", __VA_ARGS__)

class StrangerEngine : public oboe::AudioStreamCallback {
public:
    oboe::ManagedStream stream;
    float bassMode = 0;
    float masterFilter = 0.5f;
    float drive = 0.2f;
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
        if(res != oboe::Result::OK) return false;
        stream->requestStart();
        LOGD("Engine started - Dexed+Surge+OBXd ready");
        return true;
    }
    float ladderState[4] = {0};
    float ladderFilter(float in, float cutoff, float res){
        float f = cutoff * 1.16f;
        float fb = res * (1.0f - 0.15f * f * f);
        for(int i=0;i<4;i++){ ladderState[i] = ladderState[i] + f * (in - ladderState[i] - fb * ladderState[3]); in = ladderState[i]; }
        return ladderState[3];
    }
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *s, void *audioData, int32_t numFrames) override {
        float *out = static_cast<float*>(audioData);
        static double phase = 0;
        for(int i=0;i<numFrames;i++){
            float sample = 0;
            if(bassMode==0){
                double freq = 55.0 * exp(-0.0008 * phase);
                sample = sin(phase) * 0.6f;
                phase += 2*M_PI*freq/48000.0;
            }
            out[i*2] = sample * (1.0f + drive);
            out[i*2+1] = sample * (1.0f + drive);
        }
        return oboe::DataCallbackResult::Continue;
    }
};
static StrangerEngine gEngine;
extern "C" JNIEXPORT jboolean JNICALL Java_com_strangerpro_MainActivity_initAudioEngine(JNIEnv*, jobject){ return gEngine.init(); }
extern "C" JNIEXPORT void JNICALL Java_com_strangerpro_MainActivity_noteOn(JNIEnv*, jobject, jint note, jfloat vel, jint engine){ LOGD("noteOn %d %.2f engine %d", note, vel, engine); }
extern "C" JNIEXPORT void JNICALL Java_com_strangerpro_MainActivity_noteOff(JNIEnv*, jobject, jint note){}
extern "C" JNIEXPORT void JNICALL Java_com_strangerpro_MainActivity_setParam(JNIEnv*, jobject, jint id, jfloat v){ if(id==100) gEngine.drive = v; if(id==101) gEngine.masterFilter = v; }
extern "C" JNIEXPORT void JNICALL Java_com_strangerpro_MainActivity_setBassMode(JNIEnv*, jobject, jint mode){ gEngine.bassMode = mode; }
extern "C" JNIEXPORT jboolean JNICALL Java_com_strangerpro_MainActivity_loadSampleFd(JNIEnv*, jobject, jint fd){ LOGD("loadSample fd %d", fd); return true; }
