package com.martinho.wallpapereditor

import android.app.WallpaperManager
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ImageViewScreen() {
    val context = LocalContext.current
    val images = TempDataHolder.generatedImages
    val logoBitmap = TempDataHolder.logoBitmap
    val offsets = remember { TempDataHolder.offsets.toMutableStateList() }
    val initialOffsets = remember { offsets.toList() }

    if (images.isEmpty() || logoBitmap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Carregando imagens...")
            }
        }
        return
    }

    val pagerState = rememberPagerState(initialPage = 0, pageCount = { images.size })
    var isEditing by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val showCheck = remember { mutableStateOf(false) }
    val density = LocalDensity.current
    var showPositionDialog by remember { mutableStateOf(false) }

    fun triggerControlsVisibility() {
        showOptions = true
        coroutineScope.launch {
            delay(5000)
            showOptions = false
        }
    }

    LaunchedEffect(Unit) {
        delay(1000)
        triggerControlsVisibility()
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(state = pagerState, modifier = Modifier.fillMaxSize()) { page ->
            val image = images[page]
            val offset = remember { mutableStateOf(offsets[page]) }
            val isDragging = remember { mutableStateOf(false) }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clickable { triggerControlsVisibility() }
            ) {
                Image(
                    bitmap = image.asImageBitmap(),
                    contentDescription = "Imagem $page",
                    modifier = Modifier.fillMaxSize()
                )

                val shadowElevationPx = with(density) { if (isDragging.value) 8.dp.toPx() else 0f }

                Image(
                    bitmap = logoBitmap.asImageBitmap(),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .offset { IntOffset(offset.value.x.toInt(), offset.value.y.toInt()) }
                        .graphicsLayer(
                            scaleX = if (isDragging.value) 1.1f else 1f,
                            scaleY = if (isDragging.value) 1.1f else 1f,
                            shadowElevation = shadowElevationPx
                        )
                        .then(
                            if (isEditing) Modifier.pointerInput(Unit) {
                                detectDragGestures(
                                    onDragStart = { isDragging.value = true },
                                    onDragEnd = { isDragging.value = false },
                                    onDragCancel = { isDragging.value = false },
                                    onDrag = { change, dragAmount ->
                                        change.consume()
                                        offset.value += dragAmount
                                        offsets[page] = offset.value
                                    }
                                )
                            } else Modifier
                        )
                        .size(100.dp)
                )

                Text(
                    text = "X: ${offset.value.x.toInt()}  Y: ${offset.value.y.toInt()}",
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    if (showOptions) {
                        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            FloatingActionButton(onClick = { isEditing = !isEditing }) {
                                Icon(Icons.Default.Edit, contentDescription = "Editar")
                            }

                            FloatingActionButton(onClick = { showPositionDialog = true }) {
                                Icon(Icons.Default.Edit, contentDescription = "Ajustar Posição Manualmente")
                            }

                            FloatingActionButton(onClick = {
                                val originalOffset = initialOffsets[page]
                                offsets[page] = originalOffset
                                offset.value = originalOffset

                                val result = mergeStickerOnImage(image, logoBitmap, originalOffset)
                                WallpaperManager.getInstance(context).setBitmap(result)
                                Toast.makeText(
                                    context,
                                    "Wallpaper aplicado na posição original!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }) {
                                Icon(Icons.Default.AccountBox, contentDescription = "Rollback + Aplicar")
                            }

                            FloatingActionButton(onClick = {
                                val result = mergeStickerOnImage(image, logoBitmap, offset.value)
                                saveImageToGallery(context, result)

                                showCheck.value = true
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("Imagem salva com sucesso!", actionLabel = "OK")
                                    delay(1500)
                                    showCheck.value = false
                                }
                            }) {
                                Icon(Icons.Default.Done, contentDescription = "Salvar")
                            }
                        }
                    }

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

                if (showPositionDialog) {
                    var xInput by remember { mutableStateOf(offset.value.x.toInt().toString()) }
                    var yInput by remember { mutableStateOf(offset.value.y.toInt().toString()) }

                    AlertDialog(
                        onDismissRequest = { showPositionDialog = false },
                        confirmButton = {
                            TextButton(onClick = {
                                val newX = xInput.toFloatOrNull() ?: offset.value.x
                                val newY = yInput.toFloatOrNull() ?: offset.value.y
                                offset.value = Offset(newX, newY)
                                offsets[page] = offset.value
                                showPositionDialog = false
                            }) {
                                Text("Aplicar")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showPositionDialog = false }) {
                                Text("Cancelar")
                            }
                        },
                        title = { Text("Ajustar posição do sticker") },
                        text = {
                            Column {
                                OutlinedTextField(
                                    value = xInput,
                                    onValueChange = { xInput = it },
                                    label = { Text("Posição X") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                                OutlinedTextField(
                                    value = yInput,
                                    onValueChange = { yInput = it },
                                    label = { Text("Posição Y") },
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        }
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            repeat(pagerState.pageCount) { index ->
                val color = if (pagerState.currentPage == index) Color.DarkGray else Color.LightGray
                Box(
                    modifier = Modifier
                        .padding(2.dp)
                        .clip(CircleShape)
                        .background(color)
                        .size(12.dp)
                )
            }
        }

        if (showCheck.value) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "Check",
                tint = Color.Green,
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(80.dp)
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 16.dp)
        )
    }
}
