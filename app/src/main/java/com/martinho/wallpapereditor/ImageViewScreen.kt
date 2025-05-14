package com.martinho.wallpapereditor

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.input.pointer.pointerInput

@Composable
fun ImageViewScreen() {
    val context = LocalContext.current
    val backgroundBitmap = TempDataHolder.generatedImage
    val logoBitmap = TempDataHolder.logoBitmap

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
    var stickerOffset by remember { mutableStateOf(Offset(100f, 100f)) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(backgroundBitmap.width / backgroundBitmap.height.toFloat()),
            contentAlignment = Alignment.TopStart
        ) {
            // Imagem de fundo SEM LOGO embutido
            Image(
                bitmap = backgroundBitmap.asImageBitmap(),
                contentDescription = "Imagem de fundo",
                modifier = Modifier.fillMaxSize()
            )

            // Logo visível sempre, mas só arrastável se estiver em modo edição
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

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { isEditing = !isEditing }) {
                Text(if (isEditing) "Finalizar edição" else "Editar imagem")
            }

            Button(onClick = {
                val result = mergeStickerOnImage(backgroundBitmap, logoBitmap, stickerOffset)
                saveImageToGallery(context, result)
                Toast.makeText(context, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show()
            }) {
                Text("Salvar imagem com logo")
            }
        }
    }
}
