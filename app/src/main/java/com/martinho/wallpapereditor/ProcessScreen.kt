package com.martinho.wallpapereditor

import android.graphics.BitmapFactory
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import kotlinx.coroutines.launch
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.dp


@Composable
fun ProcessScreen(onNavigateToImageView: () -> Unit) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    var isProcessing by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        coroutineScope.launch {
            val uris = TempDataHolder.selectedImageUris
            val logoRes = TempDataHolder.logoResId

            val resizedImages = uris.mapNotNull { uri ->
                context.contentResolver.openInputStream(uri)?.use { stream ->
                    BitmapFactory.decodeStream(stream)?.let { resizeImage(context, it) }
                }
            }

            val logoRaw = logoRes?.let {
                BitmapFactory.decodeResource(context.resources, it)
            }

            val resizedLogo = logoRaw?.let { resizeLogo(context, it) }

            TempDataHolder.generatedImages = resizedImages
            TempDataHolder.logoBitmap = resizedLogo
            TempDataHolder.offsets = List(resizedImages.size) {
                resizedLogo?.let { getDefaultLogoOffset(context, it) } ?: Offset(100f, 100f)
            }.toMutableList()

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
            LoadingSpinnerComposable()
            Spacer(modifier = Modifier.height(12.dp))
            Text("Processando imagens...")
        }
    }
}


@Composable
fun LoadingSpinnerComposable() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}