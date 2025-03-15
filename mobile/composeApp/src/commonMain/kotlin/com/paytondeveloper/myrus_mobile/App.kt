package com.paytondeveloper.myrus_mobile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
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
import io.ktor.util.Identity.encode
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview

import myrus_mobile.composeapp.generated.resources.Res
import myrus_mobile.composeapp.generated.resources.compose_multiplatform

expect fun analyzeImage(img: ByteArray, callback: (Rect, Size) -> Unit)

data class Size(val width: Float, val height: Float)
data class Rect(val top: Float, val left: Float, val bottom: Float, val right: Float)
data class FaceData(val boundingBox: Rect)


expect fun CameraController.getResolution(): Size?

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
            LaunchedEffect(Unit) {
                //not proud of this.
                suspend fun runloop() {

                    val res = camController?.takePicture()
                    res?.let {
                        when (it) {
                            is ImageCaptureResult.Error -> {
                                println("error taking pic. skipping frame: ${it.exception}")
                            }
                            is ImageCaptureResult.Success -> {
                                analyzeImage(it.byteArray, { it, size ->
//                                    println("offset: ${it.top} ${it.left}")
                                    val factorY = it.top.toFloat() / size.height.toFloat()
                                    val factorX = it.left.toFloat() / size.width.toFloat()

                                    val newY = factorY * camSize!!.height.toFloat()
                                    val newX = factorX * camSize!!.width.toFloat()

                                    currentThingy = it.copy(top = newY, left = newX)
                                })
                            }
                        }
                    }
                    delay(1000)
                    runloop()
                }
                runloop()
            }
            Box(modifier = Modifier/*.drawWithCache {
                if (currentThingy != null) {
                    val roundedPolygon = RoundedPolygon(
                        numVertices = 6,
                        radius = (currentThingy!!.right - currentThingy!!.left).toFloat(),
                        centerX = currentThingy!!.left.toFloat(),
                        centerY = currentThingy!!.top.toFloat()
                    )
                    val roundedPolygonPath = roundedPolygon
                    onDrawBehind {
//                        drawPath(roundedPolygonPath, color = Color.Blue)
                        drawRect(
                            color = Color.Red,
                            topLeft = Offset(currentThingy!!.left.toFloat(), currentThingy!!.top.toFloat()),
                            size = androidx.compose.ui.geometry.Size(width = (currentThingy!!.right - currentThingy!!.left).toFloat(), height = (currentThingy!!.bottom - currentThingy!!.top).toFloat())
                        )
                    }

                } else {
                    onDrawBehind {  }
                }
            }*/) {
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
                })
                Text("Face", modifier = Modifier.offset(x = leftPx, y = topPx))
            }
        } else {
            Text("no permissions!! can't do anything :(")
        }
    }
}