package com.paytondeveloper.myrus_mobile

import android.os.Build
import com.kashif.cameraK.controller.CameraController

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()


