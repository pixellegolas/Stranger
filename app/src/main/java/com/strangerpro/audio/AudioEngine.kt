package com.strangerpro.audio

class AudioEngine {
    companion object {
        init {
            System.loadLibrary("strangerpro")
        }
    }

    external fun initEngine(sampleRate: Int)
    external fun startEngine(): Int
    external fun stopEngine()
    external fun noteOn(note: Int, velocity: Int)
    external fun noteOff(note: Int)
    external fun setEngineType(type: Int) // 0 dexed, 1 surge
    external fun setParam(paramId: Int, value: Float)
    external fun setAlgorithm(algo: Int)
    external fun setFeedback(fb: Float)

    private var started = false

    fun init(sampleRate: Int = 48000) {
        initEngine(sampleRate)
    }

    fun start(): Int {
        val r = startEngine()
        if (r == 0) started = true
        return r
    }

    fun stop() {
        if (started) {
            stopEngine()
            started = false
        }
    }
}
