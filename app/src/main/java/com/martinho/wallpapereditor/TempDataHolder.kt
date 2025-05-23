package com.martinho.wallpapereditor

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset

object TempDataHolder {
    var logoResId: Int? = null
    var logoBitmap: Bitmap? = null
    var selectedImageUris: List<Uri> = emptyList()
    var generatedImages: List<Bitmap> = emptyList()
    var offsets: MutableList<Offset> = mutableListOf()
}
