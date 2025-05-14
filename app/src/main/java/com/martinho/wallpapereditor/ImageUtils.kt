package com.martinho.wallpapereditor

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.geometry.Offset
import java.io.OutputStream

fun resizeImage(image: Bitmap): Bitmap {
    // Ajusta a imagem ao tamanho da tela (ou outro critério desejado)
    return Bitmap.createScaledBitmap(image, 1080, 1920, true)
}

fun resizeLogo(logo: Bitmap): Bitmap {
    return Bitmap.createScaledBitmap(logo, 100, 100, true)
}

fun mergeStickerOnImage(background: Bitmap, logo: Bitmap, offset: Offset): Bitmap {
    val result = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    canvas.drawBitmap(logo, offset.x, offset.y, null)
    return result
}

fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "sticker_image_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }
    val uri: Uri? = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    )
    uri?.let {
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
}
