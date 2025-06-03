package com.martinho.wallpapereditor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.compose.ui.geometry.Offset
import java.io.OutputStream

fun getDeviceAdjustedSize(context: Context): Pair<Int, Int> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(displayMetrics)
    return Pair(displayMetrics.widthPixels, displayMetrics.heightPixels)
}

fun resizeImage(context: Context, image: Bitmap): Bitmap {
    val (targetWidth, targetHeight) = getDeviceAdjustedSize(context)
    return Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
}

fun resizeLogo(context: Context, logo: Bitmap): Bitmap {
    val (screenWidth, _) = getDeviceAdjustedSize(context)
    val targetWidth = (screenWidth * 0.25).toInt()
    val aspectRatio = logo.height.toFloat() / logo.width
    val targetHeight = (targetWidth * aspectRatio).toInt()
    return Bitmap.createScaledBitmap(logo, targetWidth, targetHeight, true)
}

fun mergeStickerOnImage(background: Bitmap, logo: Bitmap, offset: Offset): Bitmap {
    val result = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    canvas.drawBitmap(logo, offset.x, offset.y, null)
    return result
}

fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "sticker_image_${System.currentTimeMillis()}.jpg"
    val contentValues = android.content.ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/WallpaperEditor")
    }

    val uri: Uri? = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
        contentValues
    )

    uri?.let {
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }

    return uri
}
