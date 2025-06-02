package com.martinho.wallpapereditor

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch

@Composable
fun ProcessScreen(onNavigateToImageView: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val isProcessing = remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val uris = TempDataHolder.selectedImageUris
            val logoRes = TempDataHolder.logoResId

            val resizedImages = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    val originalBitmap = BitmapFactory.decodeStream(stream)
                    originalBitmap?.let { resizeImage(context, it) }
                }
            }

            val logoRaw = logoRes?.let {
                BitmapFactory.decodeResource(context.resources, it)
            }
            val resizedLogo = logoRaw?.let { resizeLogo(context, it) }

            TempDataHolder.generatedImages = resizedImages
            TempDataHolder.logoBitmap = resizedLogo
            TempDataHolder.offsets = resizedImages.map {
                resizedLogo?.let { getDefaultLogoOffset(context, it) } ?: Offset(100f, 100f)
            }.toMutableList()

            isProcessing.value = false
            onNavigateToImageView()
        }
    }

    if (isProcessing.value) {
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
