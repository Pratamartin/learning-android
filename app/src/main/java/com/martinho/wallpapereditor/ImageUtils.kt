package com.martinho.wallpapereditor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.DisplayMetrics
import androidx.compose.ui.geometry.Offset
import java.io.OutputStream

fun resizeImage(context: Context, image: Bitmap): Bitmap {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    return Bitmap.createScaledBitmap(image, metrics.widthPixels, metrics.heightPixels, true)
}

fun resizeLogo(context: Context, logo: Bitmap): Bitmap {
    val metrics: DisplayMetrics = context.resources.displayMetrics
    val targetWidth = (metrics.widthPixels * 0.25).toInt() // 25% da largura da tela
    val aspectRatio = logo.height.toFloat() / logo.width
    val targetHeight = (targetWidth * aspectRatio).toInt()
    return Bitmap.createScaledBitmap(logo, targetWidth, targetHeight, true)
}

fun getDefaultLogoOffset(context: Context, logo: Bitmap): Offset {
    val metrics = context.resources.displayMetrics
    val margin = 16
    val x = (metrics.widthPixels - logo.width - margin).toFloat()
    val y = (metrics.heightPixels - logo.height - 250).toFloat() // acima da dock
    return Offset(x, y)
}

fun mergeStickerOnImage(background: Bitmap, logo: Bitmap, offset: Offset): Bitmap {
    val result = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    canvas.drawBitmap(logo, offset.x, offset.y, null)
    return result
}

fun saveImageToGallery(context: Context, bitmap: Bitmap): Uri? {
    val filename = "sticker_image_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
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