package com.martinho.wallpapereditor

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.martinho.wallpapereditor.ui.theme.WallpaperEditorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            WallpaperEditorTheme  {
                Surface(modifier = Modifier.fillMaxSize()) {
                    NavigationController()
                }
            }
        }
    }
}

@Composable
fun NavigationController() {
    val navController: NavHostController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") { MainScreen { navController.navigate("home") } }
        composable("home") { HomeScreen { navController.navigate("process") } }
        composable("process") { ProcessScreen { navController.navigate("imageview") } }
        composable("imageview") { ImageViewScreen() }
    }
}
