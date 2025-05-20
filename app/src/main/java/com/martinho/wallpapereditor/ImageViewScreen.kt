package com.martinho.wallpapereditor

import android.app.WallpaperManager
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset

@Composable
fun ImageViewScreen() {
    val context = LocalContext.current
    val backgroundBitmap = TempDataHolder.generatedImage
    val logoBitmap = TempDataHolder.logoBitmap
    val initialOffset = TempDataHolder.defaultOffset ?: Offset(100f, 100f)

    if (backgroundBitmap == null || logoBitmap == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingSpinnerComposable()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Carregando imagem...")
            }
        }
        return
    }

    var isEditing by remember { mutableStateOf(false) }
    var stickerOffset by remember { mutableStateOf(initialOffset) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .aspectRatio(backgroundBitmap.width / backgroundBitmap.height.toFloat()),
            contentAlignment = Alignment.TopStart
        ) {
            Image(
                bitmap = backgroundBitmap.asImageBitmap(),
                contentDescription = "Imagem de fundo",
                modifier = Modifier.fillMaxSize()
            )

            Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "Logo",
                modifier = Modifier
                    .offset {
                        IntOffset(stickerOffset.x.toInt(), stickerOffset.y.toInt())
                    }
                    .then(
                        if (isEditing) Modifier.pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consume()
                                stickerOffset += dragAmount
                            }
                        } else Modifier
                    )
                    .size(100.dp)
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { isEditing = !isEditing }) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Editar logo"
                )
            }

            IconButton(onClick = {
                val result = mergeStickerOnImage(backgroundBitmap, logoBitmap, stickerOffset)
                saveImageToGallery(context, result)
                Toast.makeText(context, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show()
            }) {
                Icon(
                    imageVector = Icons.Default.Done,
                    contentDescription = "Salvar imagem"
                )
            }

            IconButton(onClick = {
                val result = mergeStickerOnImage(backgroundBitmap, logoBitmap, stickerOffset)
                try {
                    val wm = WallpaperManager.getInstance(context)
                    wm.setBitmap(result)
                    Toast.makeText(context, "Wallpaper aplicado!", Toast.LENGTH_SHORT).show()
                } catch (e: Exception) {
                    Toast.makeText(context, "Erro ao aplicar wallpaper", Toast.LENGTH_SHORT).show()
                }
            }) {
                Icon(
                    imageVector = Icons.Default.Create,
                    contentDescription = "Aplicar wallpaper"
                )
            }
        }
    }
}
