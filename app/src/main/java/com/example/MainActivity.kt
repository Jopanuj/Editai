package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.screens.EditorScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProjectsScreen
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.StudioBackground
import com.example.ui.theme.StudioSurface
import com.example.ui.theme.StudioSurfaceBorder
import com.example.viewmodel.ShortyViewModel

enum class AppScreen(val label: String) {
    HOME("Footage"),
    EDITOR("Studio"),
    PROJECTS("Library")
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                val viewModel: ShortyViewModel = viewModel()
                var currentScreen by remember { mutableStateOf(AppScreen.HOME) }
                val notification by viewModel.notification.collectAsState()
                val snackbarHostState = remember { SnackbarHostState() }

                LaunchedEffect(notification) {
                    notification?.let {
                        snackbarHostState.showSnackbar(
                            message = it.message,
                            duration = SnackbarDuration.Short
                        )
                        viewModel.clearNotification()
                    }
                }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    containerColor = StudioBackground,
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    bottomBar = {
                        NavigationBar(
                            modifier = Modifier
                                .navigationBarsPadding()
                                .testTag("bottom_nav_bar"),
                            containerColor = StudioSurface,
                            contentColor = Color.White,
                            tonalElevation = 8.dp
                        ) {
                            NavigationBarItem(
                                selected = currentScreen == AppScreen.HOME,
                                onClick = { currentScreen = AppScreen.HOME },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.HOME) Icons.Filled.VideoLibrary else Icons.Outlined.VideoLibrary,
                                        contentDescription = "Footage"
                                    )
                                },
                                label = { Text("Footage") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = ElectricViolet
                                ),
                                modifier = Modifier.testTag("nav_footage")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.EDITOR,
                                onClick = { currentScreen = AppScreen.EDITOR },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.EDITOR) Icons.Filled.Movie else Icons.Outlined.Movie,
                                        contentDescription = "Studio"
                                    )
                                },
                                label = { Text("Studio") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = ElectricViolet
                                ),
                                modifier = Modifier.testTag("nav_studio")
                            )

                            NavigationBarItem(
                                selected = currentScreen == AppScreen.PROJECTS,
                                onClick = { currentScreen = AppScreen.PROJECTS },
                                icon = {
                                    Icon(
                                        imageVector = if (currentScreen == AppScreen.PROJECTS) Icons.Filled.FolderCopy else Icons.Outlined.FolderCopy,
                                        contentDescription = "Library"
                                    )
                                },
                                label = { Text("Library") },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = Color.White,
                                    selectedTextColor = NeonCyan,
                                    indicatorColor = ElectricViolet
                                ),
                                modifier = Modifier.testTag("nav_library")
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            label = "screen_transition"
                        ) { screen ->
                            when (screen) {
                                AppScreen.HOME -> {
                                    HomeScreen(
                                        viewModel = viewModel,
                                        onNavigateToEditor = { currentScreen = AppScreen.EDITOR }
                                    )
                                }
                                AppScreen.EDITOR -> {
                                    EditorScreen(
                                        viewModel = viewModel,
                                        onNavigateBack = { currentScreen = AppScreen.HOME }
                                    )
                                }
                                AppScreen.PROJECTS -> {
                                    ProjectsScreen(
                                        viewModel = viewModel,
                                        onOpenProject = {
                                            currentScreen = AppScreen.EDITOR
                                        },
                                        onNewProject = {
                                            currentScreen = AppScreen.HOME
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
