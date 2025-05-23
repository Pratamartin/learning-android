package com.martinho.wallpapereditor

import android.app.WallpaperManager
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.IntOffset
import com.google.accompanist.pager.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalPagerApi::class, ExperimentalFoundationApi::class)
@Composable
fun ImageViewScreen() {
    val context = LocalContext.current
    val images = TempDataHolder.generatedImages
    val logoBitmap = TempDataHolder.logoBitmap
    val offsets = remember { TempDataHolder.offsets.toMutableStateList() }

    if (images.isEmpty() || logoBitmap == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                LoadingSpinnerComposable()
                Spacer(modifier = Modifier.height(8.dp))
                Text("Carregando imagens...")
            }
        }
        return
    }

    val pagerState = rememberPagerState()
    var isEditing by remember { mutableStateOf(false) }
    var showOptions by remember { mutableStateOf(true) }
    val coroutineScope = rememberCoroutineScope()

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
        HorizontalPager(count = images.size, state = pagerState) { page ->
            val image = images[page]
            val offset = remember { mutableStateOf(offsets[page]) }

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

                Image(
                    bitmap = logoBitmap.asImageBitmap(),
                    contentDescription = "Logo",
                    modifier = Modifier
                        .offset { IntOffset(offset.value.x.toInt(), offset.value.y.toInt()) }
                        .then(
                            if (isEditing) Modifier.pointerInput(Unit) {
                                detectDragGestures { change, dragAmount ->
                                    change.consume()
                                    offset.value += dragAmount
                                    offsets[page] = offset.value
                                }
                            } else Modifier
                        )
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
            }
        }

        // Botões flutuantes
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp),
            horizontalAlignment = Alignment.End
        ) {
            AnimatedVisibility(visible = showOptions) {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    FloatingActionButton(onClick = { isEditing = !isEditing }) {
                        Icon(Icons.Default.Edit, contentDescription = "Editar")
                    }
                    FloatingActionButton(onClick = {
                        val img = images[pagerState.currentPage]
                        val offset = offsets[pagerState.currentPage]
                        val result = mergeStickerOnImage(img, logoBitmap, offset)
                        saveImageToGallery(context, result)
                        Toast.makeText(context, "Imagem salva!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Done, contentDescription = "Salvar")
                    }
                    FloatingActionButton(onClick = {
                        val img = images[pagerState.currentPage]
                        val offset = offsets[pagerState.currentPage]
                        val result = mergeStickerOnImage(img, logoBitmap, offset)
                        WallpaperManager.getInstance(context).setBitmap(result)
                        Toast.makeText(context, "Wallpaper aplicado!", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Default.Create, contentDescription = "Aplicar")
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

        // Indicador do pager
        HorizontalPagerIndicator(
            pagerState = pagerState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        )
    }
}