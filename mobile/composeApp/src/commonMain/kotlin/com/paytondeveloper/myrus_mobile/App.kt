package com.paytondeveloper.myrus_mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalViewConfiguration
import androidx.compose.ui.unit.dp
import androidx.graphics.shapes.RoundedPolygon
import com.kashif.cameraK.builder.CameraControllerBuilder
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.CameraLens
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.enums.ImageFormat
import com.kashif.cameraK.permissions.providePermissions
import com.kashif.cameraK.result.ImageCaptureResult
import com.kashif.cameraK.ui.CameraPreview
import dev.shreyaspatil.ai.client.generativeai.GenerativeModel
import dev.shreyaspatil.ai.client.generativeai.type.content
import io.ktor.util.Identity.encode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import myrus_mobile.composeapp.generated.resources.Res
import myrus_mobile.composeapp.generated.resources.compose_multiplatform
import nl.marc_apps.tts.TextToSpeechEngine
import nl.marc_apps.tts.rememberTextToSpeechOrNull

expect fun analyzeImage(img: ByteArray, callback: (Rect, Size) -> Unit)

data class Size(val width: Float, val height: Float)
data class Rect(val top: Float, val left: Float, val bottom: Float, val right: Float)
data class FaceData(val boundingBox: Rect)

expect suspend fun sayText(text: String)

val genAI = GenerativeModel(
    "gemini-2.0-flash",
    apiKey = "AIzaSyCy56R6_T3Neu54W45MMSTGpXFEb92V2yI"
)

expect fun epochMillis(): Long


@Composable
@Preview
fun App() {
    MaterialTheme {
        val permissions = providePermissions()
        val camPermission = remember { mutableStateOf(permissions.hasCameraPermission()) }
        if (!camPermission.value) {
            permissions.RequestCameraPermission( {
                camPermission.value = true
            }, onDenied = {
                camPermission.value = false
            })
        }



        if (camPermission.value) {
            var camController by remember { mutableStateOf<CameraController?>(null) }
            var camSize by remember { mutableStateOf<Size?>(null) }
            var currentThingy by remember { mutableStateOf<Rect?>(Rect(0f,0f,0f,0f)) }
            var delayMillis by remember { mutableStateOf(1000) }
            var analyzing by remember { mutableStateOf(true) }
            val tts = rememberTextToSpeechOrNull(TextToSpeechEngine.Google)
            LaunchedEffect(Unit) {
                //not proud of this.
                suspend fun roast(image: ByteArray) {
                    var content = content {
                        image(image)
                        text("make a shakespearean insult for the person in the middle of the image. return only the insult. be specific to the person in the image")
                    }
                    val res = genAI.generateContent(content)
                    println("RES: ${res.text} TTS: ${tts}")
//                    tts?.let { tts ->
//                        tts.say(res.text ?: "uh oh its broken", true)
//                    }
                    sayText(res.text ?: "uh oh its borken")
                    analyzing = true
                }
                suspend fun runloop() {

                    if (analyzing) {

                        val res = camController?.takePicture()
                        res?.let {
                            when (it) {
                                is ImageCaptureResult.Error -> {
                                    println("error taking pic. skipping frame: ${it.exception}")
                                }
                                is ImageCaptureResult.Success -> {
                                    analyzeImage(it.byteArray, { bounds, size ->
//                                    println("offset: ${it.top} ${it.left}")
                                        val factorY = bounds.top / size.height
                                        val factorX = bounds.left / size.width

                                        val newY = factorY * camSize!!.height
                                        val newX = factorX * camSize!!.width

                                        currentThingy = bounds.copy(top = newY, left = newX)
                                        analyzing = false
                                        CoroutineScope(Dispatchers.IO).launch {
                                            roast(it.byteArray)
                                        }
                                    })

                                }
                            }
                        }
                    }
                    delay(delayMillis.toLong())
                    runloop()
                }

                runloop()
            }
            Box(modifier = Modifier) {
                val topPx = with(LocalDensity.current) {
                    currentThingy!!.top.toDp()
                }
                val leftPx = with(LocalDensity.current) {
                    currentThingy!!.left.toDp()
                }
                println("offset (dp) ${topPx} ${leftPx}")

                CameraPreview(modifier = Modifier.fillMaxSize().onSizeChanged {
                    camSize = Size(
                        width = it.width.toFloat(),
                        height = it.height.toFloat()
                    )
                    println("camsize: ${camSize?.width}x${camSize?.height}")
                }, {
                    setCameraLens(CameraLens.FRONT)
                    setImageFormat(ImageFormat.PNG)
                    setDirectory(Directory.PICTURES)
                }, onCameraControllerReady = {
                    camController = it
                    if (getPlatform().name.contains("iOS")) {
                        camController!!.toggleCameraLens()
                    }
                })
                Text("Face", modifier = Modifier.offset(x = leftPx, y = topPx))
            }
            Slider(modifier = Modifier.padding(top = 64.dp), value = delayMillis.toFloat(), onValueChange = {
                delayMillis = it.toInt()
            }, valueRange = 16.67f..5000f)
        } else {
            Text("no permissions!! can't do anything :(")
        }
    }
}