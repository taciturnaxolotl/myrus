package com.paytondeveloper.myrus_mobile

import android.app.Activity
import android.content.ContentValues
import android.content.Context
import android.content.Context.CAMERA_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.util.SparseIntArray
import android.view.Surface
import androidx.annotation.RequiresApi
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.impl.CameraInternal
import androidx.camera.core.impl.Config
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import com.google.mlkit.vision.face.FaceDetectorOptions.LandmarkMode
import com.kashif.cameraK.builder.CameraControllerBuilder
import com.kashif.cameraK.controller.CameraController
import com.kashif.cameraK.enums.Directory
import com.kashif.cameraK.utils.getActivityOrNull
import org.slf4j.MDC.put
import java.io.FileOutputStream
import java.io.IOException

actual fun CameraController.getResolution(): Size? {
    val field = this::class.java.getDeclaredField("imageCapture")
    field.isAccessible = true
    val imgCapture = field.get(this) as ImageCapture?
    val res = imgCapture?.resolutionInfo?.resolution
    return if (res != null) Size(width = res.width.toFloat(), height = res.height.toFloat()) else null

}

actual fun analyzeImage(img: ByteArray, callback: (Rect, Size) -> Unit) {
//    println("res2: ${saveByteArrayToMediaStore(AppInfo.app.applicationContext, img)}")
    try {

        val rotationDegrees = rotationDegrees(AppInfo.activity, isFrontFacing = true)
//        val img = InputImage.fromByteArray(img, options.outWidth, options.outHeight, rotationDegrees, InputImage.IMAGE_FORMAT_NV21)
        val bitmap = BitmapFactory.decodeByteArray(img, 0, img.size)
        val img = InputImage.fromBitmap(bitmap, 0)
        val detector = FaceDetection.getClient()
        if (AppInfo.canDetectFace) {
            AppInfo.canDetectFace = false
            val res = detector.process(img)
            res.addOnCompleteListener {
                println("ml completed: ${it.isSuccessful} ${it.exception} ${res.result.count()}")
                AppInfo.canDetectFace = true
                res.result.forEach {
                    println("FACE @ ${it.boundingBox.top}")
                    
                    callback(Rect(it.boundingBox.top.toFloat(), it.boundingBox.left.toFloat(), it.boundingBox.bottom.toFloat(), it.boundingBox.right.toFloat()), Size(width = bitmap.width.toFloat(), height = bitmap.height.toFloat()))
                }
            }
        }

    } catch (e: Exception) {
        println("error: ${e}")
    }
}



@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
@Throws(CameraAccessException::class)
fun rotationDegrees(activity: Activity, isFrontFacing: Boolean): Int {
    val ORIENTATIONS = SparseIntArray()
    ORIENTATIONS.append(Surface.ROTATION_0, 0)
    ORIENTATIONS.append(Surface.ROTATION_90, 90)
    ORIENTATIONS.append(Surface.ROTATION_180, 180)
    ORIENTATIONS.append(Surface.ROTATION_270, 270)

    /**
     * Get the angle by which an image must be rotated given the device's current
     * orientation.
     */

    // Get the device's current rotation relative to its "native" orientation.
    // Then, from the ORIENTATIONS table, look up the angle the image must be
    // rotated to compensate for the device's rotation.
    val deviceRotation = activity.windowManager.defaultDisplay.rotation
    var rotationCompensation = ORIENTATIONS.get(deviceRotation)

    // Get the device's sensor orientation.
    val cameraManager = activity.getSystemService(CAMERA_SERVICE) as CameraManager
    var cameraId = ""
    cameraManager.cameraIdList.forEach {
        if (cameraManager.getCameraCharacteristics(it).get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) cameraId = it
    }
    val sensorOrientation = cameraManager
        .getCameraCharacteristics(cameraId)
        .get(CameraCharacteristics.SENSOR_ORIENTATION)!!

    if (isFrontFacing) {
        rotationCompensation = (sensorOrientation + rotationCompensation) % 360
    } else { // back-facing
        rotationCompensation = (sensorOrientation - rotationCompensation + 360) % 360
    }
    return rotationCompensation
}

//DEBUG ONLY

fun saveByteArrayToMediaStore(
    context: Context,
    byteArray: ByteArray,
    filename: String = "image_${System.currentTimeMillis()}.jpg",
    mimeType: String = "image/png",
    quality: Int = 100
): Uri? {
    // First decode the image to get its dimensions
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true  // This option decodes only the dimensions without loading the full bitmap
    }

    BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size, options)

    // Check if we could decode the image dimensions
    if (options.outWidth <= 0 || options.outHeight <= 0) {
        return null  // Invalid image data
    }

    // Now decode the actual bitmap
    val bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
        ?: return null  // Failed to decode

    // Set up the MediaStore ContentValues
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        put(MediaStore.Images.Media.WIDTH, bitmap.width)
        put(MediaStore.Images.Media.HEIGHT, bitmap.height)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
    }

    // Insert and get the URI
    val contentResolver = context.contentResolver
    val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)

    uri?.let {
        try {
            contentResolver.openOutputStream(it)?.use { outputStream ->
                bitmap.compress(
                    when (mimeType) {
                        "image/png" -> Bitmap.CompressFormat.PNG
                        "image/webp" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                            Bitmap.CompressFormat.WEBP_LOSSLESS
                        } else {
                            @Suppress("DEPRECATION")
                            Bitmap.CompressFormat.WEBP
                        }
                        else -> Bitmap.CompressFormat.JPEG
                    },
                    quality,
                    outputStream
                )
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                contentValues.clear()
                contentValues.put(MediaStore.Images.Media.IS_PENDING, 0)
                contentResolver.update(it, contentValues, null, null)
            }
        } catch (e: IOException) {
            contentResolver.delete(it, null, null)
            return null
        }
    }

    bitmap.recycle()
    return uri
}