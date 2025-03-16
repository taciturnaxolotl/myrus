package com.paytondeveloper.myrus_mobile

import androidx.compose.runtime.Composable
import androidx.compose.ui.interop.LocalUIViewController
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.usePinned
import platform.Foundation.NSData
import platform.Foundation.NSDate
import platform.Foundation.timeIntervalSince1970
import platform.UIKit.UIGraphicsImageRenderer
import platform.posix.memcpy

fun MainViewController() = ComposeUIViewController { App() }

actual fun epochMillis(): Long = (NSDate().timeIntervalSince1970 * 1000).toLong()

@OptIn(ExperimentalForeignApi::class)
fun NSDataToByteArray(data: NSData):  ByteArray = ByteArray(data.length.toInt()).apply {
    usePinned {
        memcpy(it.addressOf(0), data.bytes, data.length)
    }
}

actual suspend fun sayText(text: String) {}