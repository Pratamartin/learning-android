package com.martinho.wallpapereditor

import android.app.WallpaperManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    var showOptions by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    var autoHideJob by remember { mutableStateOf<Job?>(null) }

    fun triggerControlsVisibility() {
        showOptions = true
        autoHideJob?.cancel()
        autoHideJob = coroutineScope.launch {
            delay(5000)
            showOptions = false
        }
    }

    LaunchedEffect(Unit) {
        delay(1000) // mostra as opções 1s depois
        showOptions = true
    }

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
                .aspectRatio(backgroundBitmap.width / backgroundBitmap.height.toFloat())
                .clickable { triggerControlsVisibility() },
            contentAlignment = Alignment.TopStart
        ) {
            // Imagem de fundo
            Image(
                bitmap = backgroundBitmap.asImageBitmap(),
                contentDescription = "Imagem de fundo",
                modifier = Modifier.fillMaxSize()
            )

            // Logo (sticker)
            Image(
                bitmap = logoBitmap.asImageBitmap(),
                contentDescription = "Logo",
                modifier = Modifier
                    .offset { IntOffset(stickerOffset.x.toInt(), stickerOffset.y.toInt()) }
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

            Surface(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(20.dp),
                color = Color.Black.copy(alpha = 0.6f),
                shape = RoundedCornerShape(8.dp),
                tonalElevation = 4.dp
            ) {
                Text(
                    text = "X: ${stickerOffset.x.toInt()}  Y: ${stickerOffset.y.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }


            // Botões flutuantes dentro da imagem
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            ) {
                Column(horizontalAlignment = Alignment.End) {
                    AnimatedVisibility(visible = showOptions) {
                        Column(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            horizontalAlignment = Alignment.End
                        ) {
                            FloatingActionButton(
                                onClick = { isEditing = !isEditing },
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            FloatingActionButton(
                                onClick = {
                                    val result = mergeStickerOnImage(backgroundBitmap, logoBitmap, stickerOffset)
                                    saveImageToGallery(context, result)
                                    Toast.makeText(context, "Imagem salva com sucesso!", Toast.LENGTH_SHORT).show()
                                }
                            ) {
                                Icon(Icons.Default.Done, contentDescription = "Salvar")
                            }

                            FloatingActionButton(
                                onClick = {
                                    val result = mergeStickerOnImage(backgroundBitmap, logoBitmap, stickerOffset)
                                    try {
                                        val wm = WallpaperManager.getInstance(context)
                                        wm.setBitmap(result)
                                        Toast.makeText(context, "Wallpaper aplicado!", Toast.LENGTH_SHORT).show()
                                    } catch (e: Exception) {
                                        Toast.makeText(context, "Erro ao aplicar wallpaper", Toast.LENGTH_SHORT).show()
                                    }
                                }
                            ) {
                                Icon(Icons.Default.Create, contentDescription = "Aplicar")
                            }
                        }
                    }

                    // Botão principal que abre/fecha o menu
                    FloatingActionButton(onClick = {
                        showOptions = !showOptions
                        if (showOptions) triggerControlsVisibility()
                    }) {
                        Icon(
                            imageVector = if (showOptions) Icons.Default.Close else Icons.Default.Edit,
                            contentDescription = "Menu"
                        )
                    }
                }
            }
        }
    }
}