package com.strangerpro

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MotionEvent
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts

class MainActivity : ComponentActivity() {
    private lateinit var webView: WebView
    external fun initAudioEngine(): Boolean
    external fun noteOn(note: Int, velocity: Float, engine: Int)
    external fun noteOff(note: Int)
    external fun setParam(paramId: Int, value: Float)
    external fun setBassMode(mode: Int)
    external fun loadSampleFd(fd: Int): Boolean

    companion object { init { try { System.loadLibrary("stranger-engine") } catch(e:Exception){} } }

    private val samplePicker = registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri: Uri? ->
        uri?.let {
            try {
                val fd = contentResolver.openFileDescriptor(it, "r")?.detachFd() ?: -1
                if(fd != -1) loadSampleFd(fd)
                webView.evaluateJavascript("window.onSampleLoaded && window.onSampleLoaded('${it.lastPathSegment}');", null)
            } catch(e:Exception){}
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        try { initAudioEngine() } catch(e:Exception){}
        webView = WebView(this).apply {
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            settings.allowFileAccess = true
            webViewClient = WebViewClient()
            addJavascriptInterface(object {
                @android.webkit.JavascriptInterface fun noteOnJS(n:Int, v:Float, eng:Int){ noteOn(n, v, eng) }
                @android.webkit.JavascriptInterface fun noteOffJS(n:Int){ noteOff(n) }
                @android.webkit.JavascriptInterface fun setParamJS(id:Int, v:Float){ setParam(id, v) }
                @android.webkit.JavascriptInterface fun setBassModeJS(m:Int){ setBassMode(m) }
                @android.webkit.JavascriptInterface fun pickSample(){ samplePicker.launch(arrayOf("audio/*")) }
                @android.webkit.JavascriptInterface fun getSPenInfo(): String { return "ready" }
            }, "Android")
            loadUrl("file:///android_asset/stranger-pro-final.html")
        }
        setContentView(webView)
    }

    // S-Pen handling - Tab S8 Ultra 4096 pressure levels
    override fun onTouchEvent(event: MotionEvent): Boolean {
        val isPen = event.getToolType(0) == MotionEvent.TOOL_TYPE_STYLUS
        if(isPen) {
            val pressure = event.pressure // 0..1
            val tiltX = event.getAxisValue(MotionEvent.AXIS_TILT_X) * 90f
            val tiltY = event.getAxisValue(MotionEvent.AXIS_TILT_Y)
            val isEraser = (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0
            setParam(100, pressure) // fine tune + velocity
            setParam(101, tiltX) // filter
            setParam(102, if(isEraser) 1f else 0f)
            webView.evaluateJavascript("window.onSPen && window.onSPen(${pressure}, ${tiltX}, ${tiltY}, ${isEraser});", null)
        }
        return super.onTouchEvent(event)
    }
}
