package com.paytondeveloper.myrus_mobile

import com.kashif.cameraK.builder.CameraControllerBuilder
import com.kashif.cameraK.controller.CameraController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.toCValues
import platform.Foundation.NSData
import platform.Foundation.create

object NativeAnalyzer {
    lateinit var analyzeImageNative: (img: ByteArray, callback: (Rect, Size) -> Unit) -> Unit
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    fun byteArrayToData(byteArray: ByteArray): NSData = memScoped {
        return NSData.create(
            bytes = byteArray.toCValues().getPointer(this),
            length = byteArray.size.toULong()
        )
    }
}

actual fun analyzeImage(img: ByteArray, callback:(Rect, Size) -> Unit) {
    NativeAnalyzer.analyzeImageNative(img, callback)
}


