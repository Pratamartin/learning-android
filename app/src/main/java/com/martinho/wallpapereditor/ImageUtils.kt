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

// Pega as dimensões da tela (considera dobráveis também)
fun getDeviceAdjustedSize(context: Context): Pair<Int, Int> {
    val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
    val displayMetrics = DisplayMetrics()
    wm.defaultDisplay.getMetrics(displayMetrics)

    val screenWidth = displayMetrics.widthPixels
    val screenHeight = displayMetrics.heightPixels

    return Pair(screenWidth, screenHeight)
}

// Redimensiona a imagem de fundo para caber na tela
fun resizeImage(context: Context, image: Bitmap): Bitmap {
    val (targetWidth, targetHeight) = getDeviceAdjustedSize(context)
    return Bitmap.createScaledBitmap(image, targetWidth, targetHeight, true)
}

// Redimensiona o logo proporcionalmente (25% da largura da tela)
fun resizeLogo(context: Context, logo: Bitmap): Bitmap {
    val (screenWidth, _) = getDeviceAdjustedSize(context)
    val targetWidth = (screenWidth * 0.25).toInt()
    val aspectRatio = logo.height.toFloat() / logo.width
    val targetHeight = (targetWidth * aspectRatio).toInt()
    return Bitmap.createScaledBitmap(logo, targetWidth, targetHeight, true)
}

// Calcula a posição do sticker exatamente no Hotseat (~15% da tela)
fun getDefaultLogoOffset(context: Context, logo: Bitmap): Offset {
    val (screenWidth, screenHeight) = getDeviceAdjustedSize(context)

    val hotseatHeight = (screenHeight * 0.15).toInt() // Hotseat estimado em 15%
    val x = (screenWidth - logo.width) / 2f // Centralizado horizontalmente
    val y = (screenHeight - hotseatHeight + (hotseatHeight - logo.height) / 2f) // Centralizado verticalmente no Hotseat

    return Offset(x, y)
}

// Mescla o sticker na imagem
fun mergeStickerOnImage(background: Bitmap, logo: Bitmap, offset: Offset): Bitmap {
    val result = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    canvas.drawBitmap(logo, offset.x, offset.y, null)
    return result
}

// Salva a imagem final
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
