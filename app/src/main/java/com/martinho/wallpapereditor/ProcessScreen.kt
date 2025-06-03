package com.martinho.wallpapereditor

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.json.JSONObject
import java.io.File
import androidx.compose.ui.geometry.Offset

@Composable
fun ProcessScreen(onNavigateToImageView: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val uriList = TempDataHolder.selectedImageUris
            val logoRes = TempDataHolder.logoResId

            // 1️⃣ Lê as bounds reais do hotseat do JSON
            val hotseatBounds = readHotseatBoundsFromJson()

            // 2️⃣ Processa as imagens
            val resizedImages = uriList.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.let { resizeImage(context, it) }
                }
            }

            val logoBitmap = logoRes?.let {
                BitmapFactory.decodeResource(context.resources, it)
            }?.let { resizeLogo(context, it) }

            // 3️⃣ Calcula offsets reais para o Hotseat real
            val offsets = resizedImages.map {
                hotseatBounds?.let {
                    Offset(
                        (hotseatBounds.left + hotseatBounds.right - logoBitmap!!.width) / 2f,
                        (hotseatBounds.top + hotseatBounds.bottom - logoBitmap.height) / 2f
                    )
                } ?: Offset(100f, 100f)
            }

            // 4️⃣ Atualiza o TempDataHolder
            TempDataHolder.generatedImages = resizedImages
            TempDataHolder.logoBitmap = logoBitmap
            TempDataHolder.offsets = offsets.toMutableList()

            isProcessing = false
            onNavigateToImageView()
        }
    }

    if (isProcessing) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CircularProgressIndicator()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Gerando visualização...")
        }
    }
}

private fun readHotseatBoundsFromJson(): android.graphics.Rect? {
    val file = File("/sdcard/device_bounds.json")
    if (!file.exists()) return null

    val jsonStr = file.readText()
    val jsonObject = JSONObject(jsonStr)
    val hotseat = jsonObject.getJSONObject("hotseat_bounds")

    return android.graphics.Rect(
        hotseat.getInt("left"),
        hotseat.getInt("top"),
        hotseat.getInt("right"),
        hotseat.getInt("bottom")
    )
}
