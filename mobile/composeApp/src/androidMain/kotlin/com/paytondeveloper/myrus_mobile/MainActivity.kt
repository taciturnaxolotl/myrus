package com.paytondeveloper.myrus_mobile

import android.R.attr.bitmap
import android.app.Activity
import android.app.Application
import android.graphics.Bitmap
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.graphics.applyCanvas
import kotlinx.coroutines.suspendCancellableCoroutine
import java.nio.ByteBuffer
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AppInfo.app = this.application
        AppInfo.activity = this
        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}

actual fun epochMillis() = System.currentTimeMillis()

actual suspend fun sayText(text: String) {
    return suspendCancellableCoroutine { continuation ->
        var tts: TextToSpeech? = null

        tts = TextToSpeech(AppInfo.app.applicationContext) { status ->
            println("init'd with status: $status")

            if (status == TextToSpeech.SUCCESS) {
                // Now the TTS instance is initialized and can be used
                tts?.let { ttsInstance ->
                    val result = ttsInstance.speak(text, TextToSpeech.QUEUE_FLUSH, null, "utteranceId")

                    if (result == TextToSpeech.SUCCESS) {
                        ttsInstance.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                            override fun onStart(utteranceId: String?) {}

                            override fun onDone(utteranceId: String?) {
                                continuation.resume(Unit)
                                ttsInstance.shutdown()
                            }

                            override fun onError(utteranceId: String?) {
                                continuation.resume(Unit)
                                ttsInstance.shutdown()
                            }
                        })
                    } else {
                        continuation.resume(Unit)
                        ttsInstance.shutdown()
                    }
                }
            } else {
                continuation.resume(Unit)
                tts?.shutdown()
            }
        }

        continuation.invokeOnCancellation {
            tts?.stop()
            tts?.shutdown()
        }
    }
}

@Composable
fun CaptureView(): ByteArray {
    val view = LocalView.current
    val context = LocalContext.current
    val bmp = Bitmap.createBitmap(view.width, view.height,
        Bitmap.Config.ARGB_8888).applyCanvas {
        view.draw(this)
    }
    bmp.let {
        val lnth: Int = bmp.getByteCount()
        val dst = ByteBuffer.allocate(lnth)
        bmp.copyPixelsToBuffer(dst)
        val barray = dst.array()
        return barray
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

object AppInfo {
    lateinit var app: Application
    lateinit var activity: Activity
    var canDetectFace: Boolean = true
}