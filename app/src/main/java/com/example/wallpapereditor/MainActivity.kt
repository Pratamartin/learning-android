package com.example.wallpapereditor

import android.content.ContentValues
import android.content.Context
import android.graphics.*
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wallpapereditor.ui.theme.WallpaperEditorTheme
import kotlinx.coroutines.launch
import java.io.OutputStream

import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.consumeAllChanges
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.foundation.Canvas
import androidx.compose.ui.graphics.StrokeCap


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperEditorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    WallpaperEditorScreen()
                }
            }
        }
    }
}

@Composable
fun WallpaperEditorScreen() {
    val context = LocalContext.current
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var originalBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var stickerBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var stickerOffset by remember { mutableStateOf(Offset.Zero) }
    var imageSize by remember { mutableStateOf(IntSize(1, 1)) }
    var isSaving by remember { mutableStateOf(false) }
    var saveMessage by remember { mutableStateOf("") }
    val coroutineScope = rememberCoroutineScope()

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        selectedImageUri = uri
        uri?.let {
            val inputStream = context.contentResolver.openInputStream(it)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            originalBitmap = bitmap
            stickerBitmap = BitmapFactory.decodeResource(context.resources, R.drawable.logoeditado)

            // Inicializa o sticker no centro da imagem
            bitmap?.let { bg ->
                stickerBitmap?.let { st ->
                    val centerX = (bg.width - st.width) / 2f
                    val centerY = (bg.height - st.height) / 2f
                    stickerOffset = Offset(centerX, centerY)
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = { launcher.launch("image/*") }) {
            Text("Selecionar imagem da galeria")
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (originalBitmap != null && stickerBitmap != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(400.dp),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    bitmap = originalBitmap!!.asImageBitmap(),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxSize()
                        .onGloballyPositioned { layoutCoordinates ->
                            imageSize = layoutCoordinates.size
                        }
                )

                Image(
                    bitmap = stickerBitmap!!.asImageBitmap(),
                    contentDescription = "Sticker",
                    modifier = Modifier
                        .offset {
                            val maxX = (imageSize.width - 100).coerceAtLeast(0)
                            val maxY = (imageSize.height - 100).coerceAtLeast(0)
                            IntOffset(
                                stickerOffset.x.toInt().coerceIn(0, maxX),
                                stickerOffset.y.toInt().coerceIn(0, maxY)
                            )
                        }
                        .pointerInput(Unit) {
                            detectDragGestures { change, dragAmount ->
                                change.consumeAllChanges()
                                stickerOffset = Offset(
                                    (stickerOffset.x + dragAmount.x).coerceIn(0f, imageSize.width - 100f),
                                    (stickerOffset.y + dragAmount.y).coerceIn(0f, imageSize.height - 100f)
                                )
                            }
                        }
                        .size(100.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (isSaving) {
                LoadingSpinnerComposable()
            } else {
                Button(onClick = {
                    isSaving = true
                    saveMessage = ""
                    coroutineScope.launch {
                        val resultBitmap = mergeStickerOnImage(
                            originalBitmap!!,
                            stickerBitmap!!,
                            stickerOffset
                        )
                        saveImageToGallery(context, resultBitmap)
                        isSaving = false
                        saveMessage = "Imagem salva com sucesso!"
                    }
                }) {
                    Text("Salvar imagem com sticker")
                }
            }

            if (saveMessage.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(saveMessage, color = MaterialTheme.colorScheme.primary)
            }
        }
    }
}

@Composable
fun LoadingSpinnerComposable(
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.primary,
    strokeWidth: Dp = 4.dp
) {
    var currentRotation by remember { mutableStateOf(0f) }

    LaunchedEffect(Unit) {
        while (true) {
            currentRotation += 30f
            kotlinx.coroutines.delay(100)
        }
    }

    Canvas(
        modifier = modifier
            .size(48.dp)
    ) {
        val sweepAngle = 270f
        val startAngle = currentRotation - 90f

        drawArc(
            color = color,
            startAngle = startAngle,
            sweepAngle = sweepAngle,
            useCenter = false,
            style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
        )
    }
}



fun saveImageToGallery(context: Context, bitmap: Bitmap) {
    val filename = "sticker_image_${System.currentTimeMillis()}.jpg"
    val contentValues = ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, filename)
        put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
        put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
    }

    val uri: Uri? = context.contentResolver.insert(
        MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues
    )

    uri?.let {
        val outputStream: OutputStream? = context.contentResolver.openOutputStream(it)
        outputStream?.use { stream ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
        }
    }
}


fun mergeStickerOnImage(background: Bitmap, sticker: Bitmap, offset: Offset): Bitmap {
    val result = background.copy(Bitmap.Config.ARGB_8888, true)
    val canvas = Canvas(result)
    canvas.drawBitmap(sticker, offset.x, offset.y, null)
    return result
}

