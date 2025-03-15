package com.paytondeveloper.myrus_mobile

import com.kashif.cameraK.builder.CameraControllerBuilder
import com.kashif.cameraK.controller.CameraController

actual fun analyzeImage(img: ByteArray, callback:(Rect, Size) -> Unit) {}

actual fun CameraController.getResolution(): Size? {return null}