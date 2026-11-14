package com.sekolah.sipena

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.WindowManager
import android.webkit.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var webView: WebView
    private var isExamLocked = false

    @SuppressLint("SetJavaScriptEnabled")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webView = WebView(this)
        setContentView(webView)

        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowFileAccess = true
            userAgentString = "Mozilla/5.0 (Linux; Android 13) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Mobile Safari/537.36"
        }

        webView.webViewClient = WebViewClient()
        webView.webChromeClient = WebChromeClient()
        webView.addJavascriptInterface(SipenaBridge(), "AndroidBridge")

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isExamLocked) {
                    Toast.makeText(this@MainActivity, "Tombol Back dinonaktifkan demi keamanan ujian SIPENA", Toast.LENGTH_SHORT).show()
                } else if (webView.canGoBack()) {
                    webView.goBack()
                }
            }
        })

        webView.loadUrl("file:///android_asset/index.html")
    }

    override fun onPause() {
        super.onPause()
        if (isExamLocked) {
            webView.evaluateJavascript("if(typeof simulateCheatAttempt==='function') simulateCheatAttempt('Keluar dari Aplikasi SIPENA');", null)
        }
    }

    inner class SipenaBridge {
        @JavascriptInterface
        fun startLockdown() = runOnUiThread {
            isExamLocked = true
            try { startLockTask() } catch (_: Exception) {}
        }

        @JavascriptInterface
        fun stopLockdown() = runOnUiThread {
            isExamLocked = false
            try { stopLockTask() } catch (_: Exception) {}
        }

        @JavascriptInterface
        fun getMediaVolumePercent(): Int {
            val am = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            val cur = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val max = am.getStreamMaxVolume(AudioManager.STREAM_MUSIC)
            return if (max > 0) (cur * 100) / max else 100
        }
    }
}
