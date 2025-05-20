package com.martinho.wallpapereditor

import android.graphics.Bitmap
import android.net.Uri
import androidx.compose.ui.geometry.Offset

object TempDataHolder {
    var logoResId: Int? = null
    var logoBitmap: Bitmap? = null
    var selectedImageUri: Uri? = null
    var generatedImage: Bitmap? = null
    var defaultOffset: Offset? = null
}
