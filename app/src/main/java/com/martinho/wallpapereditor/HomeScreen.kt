package com.martinho.wallpapereditor

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.wallpapereditor.R

@Composable
fun HomeScreen(onNavigateToProcess: () -> Unit) {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri != null && TempDataHolder.logoResId != null) {
            TempDataHolder.selectedImageUri = uri
            // O logo será carregado e redimensionado no ProcessScreen
            onNavigateToProcess()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            TempDataHolder.logoResId = R.drawable.logo_claro
            launcher.launch("image/*")
        }) {
            Text("Generate Wallpaper Claro")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(onClick = {
            TempDataHolder.logoResId = R.drawable.telcel_logo
            launcher.launch("image/*")
        }) {
            Text("Generate Wallpaper Telcel")
        }
    }
}
