#include <jni.h>
#include <android/log.h>
#include <oboe/Oboe.h>
#include "dexed_wrapper.h"
#include "surge_wrapper.h"
#include <mutex>
#include <atomic>

#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, "StrangerPro", __VA_ARGS__)

static DexedWrapper gDexed;
static SurgeWrapper gSurge;
static std::atomic<int> gEngineType(0); // 0 dexed, 1 surge
static float gSampleRate = 48000.0f;

class AudioCallback : public oboe::AudioStreamCallback {
public:
    oboe::DataCallbackResult onAudioReady(oboe::AudioStream *stream, void *audioData, int32_t numFrames) override {
        float *floatData = (float*)audioData;
        // Clear
        for (int i=0;i<numFrames;i++) floatData[i]=0.0f;

        if (gEngineType.load() == 0) {
            gDexed.render(floatData, numFrames);
        } else {
            gSurge.render(floatData, numFrames);
        }
        return oboe::DataCallbackResult::Continue;
    }
};

static std::shared_ptr<oboe::AudioStream> gStream;
static AudioCallback gCallback;
static std::mutex gStreamMutex;

extern "C" {

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_initEngine(JNIEnv* env, jobject thiz, jint sampleRate) {
    gSampleRate = sampleRate > 0 ? sampleRate : 48000;
    gDexed.init(gSampleRate);
    gSurge.init(gSampleRate);
    LOGI("Engines init sr=%.0f", gSampleRate);
}

JNIEXPORT jint JNICALL
Java_com_strangerpro_audio_AudioEngine_startEngine(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(gStreamMutex);
    if (gStream) {
        gStream->close();
        gStream.reset();
    }

    oboe::AudioStreamBuilder builder;
    builder.setDirection(oboe::Direction::Output);
    builder.setPerformanceMode(oboe::PerformanceMode::LowLatency);
    builder.setSharingMode(oboe::SharingMode::Exclusive);
    builder.setFormat(oboe::AudioFormat::Float);
    builder.setChannelCount(1);
    builder.setSampleRate((int32_t)gSampleRate);
    builder.setCallback(&gCallback);

    oboe::Result result = builder.openStream(gStream);
    if (result != oboe::Result::OK) {
        LOGI("Failed to open stream %d", (int)result);
        return -1;
    }
    result = gStream->requestStart();
    if (result != oboe::Result::OK) {
        LOGI("Failed to start stream %d", (int)result);
        return -2;
    }
    LOGI("Audio stream started");
    return 0;
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_stopEngine(JNIEnv* env, jobject thiz) {
    std::lock_guard<std::mutex> lock(gStreamMutex);
    if (gStream) {
        gStream->stop();
        gStream->close();
        gStream.reset();
        LOGI("Audio stream stopped");
    }
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_noteOn(JNIEnv* env, jobject thiz, jint note, jint velocity) {
    if (gEngineType.load()==0) gDexed.noteOn(note, velocity);
    else gSurge.noteOn(note, velocity);
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_noteOff(JNIEnv* env, jobject thiz, jint note) {
    if (gEngineType.load()==0) gDexed.noteOff(note);
    else gSurge.noteOff(note);
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_setEngineType(JNIEnv* env, jobject thiz, jint type) {
    gEngineType.store(type);
    LOGI("Engine type %d", type);
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_setParam(JNIEnv* env, jobject thiz, jint paramId, jfloat value) {
    // paramId mapping:
    // 0 master volume, 1 cutoff, 2 resonance, 3 waveType, 10-15 op levels dexed
    if (gEngineType.load()==0) {
        if (paramId>=10 && paramId<16) {
            gDexed.setOpLevel(paramId-10, value);
        }
    } else {
        if (paramId==1) gSurge.setCutoff(value);
        else if (paramId==2) gSurge.setResonance(value);
        else if (paramId==3) gSurge.setWaveType((int)value);
    }
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_setAlgorithm(JNIEnv* env, jobject thiz, jint algo) {
    gDexed.setAlgorithm(algo);
}

JNIEXPORT void JNICALL
Java_com_strangerpro_audio_AudioEngine_setFeedback(JNIEnv* env, jobject thiz, jfloat fb) {
    gDexed.setFeedback(fb);
}

} // extern C
