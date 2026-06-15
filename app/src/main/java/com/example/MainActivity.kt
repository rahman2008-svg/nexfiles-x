package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.*
import com.example.ui.theme.AmoledBlack
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.FileViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val fileViewModel: FileViewModel = viewModel()
                val currentRoute by fileViewModel.currentRoute.collectAsStateWithLifecycle()

                // Hide bottom navigation in cinema media playmodes for absolute immersion
                val isMediaMode = currentRoute.startsWith("view_")

                Scaffold(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(AmoledBlack),
                    bottomBar = {
                        if (!isMediaMode) {
                            AppBottomNavigation(
                                currentRoute = currentRoute,
                                onTabSelected = { target ->
                                    fileViewModel.navigateTo(target)
                                    fileViewModel.refreshAll()
                                }
                            )
                        }
                    },
                    containerColor = AmoledBlack
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        ActiveScreenRouter(route = currentRoute, viewModel = fileViewModel)
                    }
                }
            }
        }
    }
}

@Composable
fun ActiveScreenRouter(route: String, viewModel: FileViewModel) {
    when (route) {
        "home" -> HomeScreen(viewModel = viewModel) { categoryName ->
            // Switch current dir based on quick category filtering
            val targetFolder = viewModel.sandboxRoot.value.resolve(categoryName)
            if (targetFolder.exists()) {
                viewModel.navigateInto(targetFolder)
                viewModel.navigateTo("explorer")
            } else {
                viewModel.navigateTo("explorer")
            }
        }
        "explorer" -> ExplorerScreen(viewModel = viewModel)
        "clean" -> CleanScreen(viewModel = viewModel)
        "vault_auth" -> VaultScreen(viewModel = viewModel)
        "tools" -> ToolsScreen(viewModel = viewModel)
        "apk_manager" -> ApkManagerScreen(viewModel = viewModel)
        "share" -> ShareScreen(viewModel = viewModel)
        "about" -> AboutScreen(viewModel = viewModel)
        "view_photo" -> ViewPhotoScreen(viewModel = viewModel)
        "view_video" -> ViewVideoScreen(viewModel = viewModel)
        "view_audio" -> ViewAudioScreen(viewModel = viewModel)
        else -> HomeScreen(viewModel = viewModel) {}
    }
}
