package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.AppScreen
import com.example.ui.MainViewModel
import com.example.ui.components.StatusBanner
import com.example.ui.screens.AppSelectorScreen
import com.example.ui.screens.RosettaStoneScreen
import com.example.ui.screens.StoreCheckScreen
import com.example.ui.screens.TranspilerPlaygroundScreen
import com.example.ui.screens.XcodeStudioScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                MainAppContent()
            }
        }
    }
}

@Composable
fun MainAppContent(viewModel: MainViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsState()

    // Determine bottom nav item
    val activeNavIndex = when (uiState.currentScreen) {
        AppScreen.APP_SELECTION, AppScreen.STORE_CHECK, AppScreen.XCODE_STUDIO -> 0
        AppScreen.PLAYGROUND -> 1
        AppScreen.ROSETTA_STONE -> 2
    }

    // Top-level back handler
    BackHandler(enabled = uiState.currentScreen != AppScreen.APP_SELECTION) {
        when (uiState.currentScreen) {
            AppScreen.XCODE_STUDIO -> viewModel.navigateTo(AppScreen.STORE_CHECK)
            AppScreen.STORE_CHECK -> viewModel.navigateTo(AppScreen.APP_SELECTION)
            AppScreen.PLAYGROUND, AppScreen.ROSETTA_STONE -> viewModel.navigateTo(AppScreen.APP_SELECTION)
            AppScreen.APP_SELECTION -> { /* exit app */ }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            // Only show bottom navigation on primary screens
            if (uiState.currentScreen != AppScreen.XCODE_STUDIO) {
                NavigationBar(modifier = Modifier.testTag("main_bottom_nav")) {
                    NavigationBarItem(
                        selected = activeNavIndex == 0,
                        onClick = { viewModel.navigateTo(AppScreen.APP_SELECTION) },
                        icon = { Icon(Icons.Default.SwapHoriz, contentDescription = "Porter") },
                        label = { Text("App Porter") },
                        modifier = Modifier.testTag("nav_porter")
                    )
                    NavigationBarItem(
                        selected = activeNavIndex == 1,
                        onClick = { viewModel.navigateTo(AppScreen.PLAYGROUND) },
                        icon = { Icon(Icons.Default.AutoFixHigh, contentDescription = "Playground") },
                        label = { Text("Playground") },
                        modifier = Modifier.testTag("nav_playground")
                    )
                    NavigationBarItem(
                        selected = activeNavIndex == 2,
                        onClick = { viewModel.navigateTo(AppScreen.ROSETTA_STONE) },
                        icon = { Icon(Icons.Default.MenuBook, contentDescription = "Rosetta Stone") },
                        label = { Text("Rosetta Stone") },
                        modifier = Modifier.testTag("nav_rosetta")
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            when (uiState.currentScreen) {
                AppScreen.APP_SELECTION -> AppSelectorScreen(viewModel = viewModel)
                AppScreen.STORE_CHECK -> StoreCheckScreen(viewModel = viewModel)
                AppScreen.XCODE_STUDIO -> XcodeStudioScreen(viewModel = viewModel)
                AppScreen.PLAYGROUND -> TranspilerPlaygroundScreen(viewModel = viewModel)
                AppScreen.ROSETTA_STONE -> RosettaStoneScreen()
            }

            // Global status notifications
            uiState.statusMessage?.let { msg ->
                StatusBanner(
                    message = msg,
                    onDismiss = { viewModel.clearStatusMessage() }
                )
            }
        }
    }
}
