package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.example.ui.components.AppBottomNav
import com.example.ui.components.AppTopBar
import com.example.ui.navigation.Screen
import com.example.ui.screens.*
import com.example.ui.theme.YouTubeAutomationTheme
import com.example.ui.viewmodel.MainViewModel

class MainActivity : ComponentActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val isDarkTheme by viewModel.isDarkTheme.collectAsState()
            val currentScreen by viewModel.currentScreen.collectAsState()
            val currentLanguage by viewModel.currentLanguage.collectAsState()
            val statusMessage by viewModel.statusMessage.collectAsState()
            val snackbarHostState = remember { SnackbarHostState() }

            LaunchedEffect(statusMessage) {
                statusMessage?.let {
                    snackbarHostState.showSnackbar(it, duration = SnackbarDuration.Short)
                }
            }

            // Android BackHandler
            BackHandler(enabled = currentScreen != Screen.HOME && currentScreen != Screen.SPLASH) {
                if (!viewModel.navigateBack()) {
                    finish()
                }
            }

            YouTubeAutomationTheme(darkTheme = isDarkTheme) {
                val showNavBars = currentScreen != Screen.SPLASH &&
                        currentScreen != Screen.ONBOARDING &&
                        currentScreen != Screen.SIGN_IN

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        if (showNavBars) {
                            AppTopBar(
                                currentScreen = currentScreen,
                                language = currentLanguage,
                                isDarkTheme = isDarkTheme,
                                onBack = { viewModel.navigateBack() },
                                onToggleLang = {
                                    val nextLang = if (currentLanguage == com.example.data.model.AppLanguage.ENGLISH)
                                        com.example.data.model.AppLanguage.TAMIL
                                    else com.example.data.model.AppLanguage.ENGLISH
                                    viewModel.setLanguage(nextLang)
                                },
                                onToggleTheme = { viewModel.toggleTheme() },
                                onOpenSettings = { viewModel.navigateTo(Screen.SETTINGS) }
                            )
                        }
                    },
                    bottomBar = {
                        if (showNavBars) {
                            AppBottomNav(
                                currentScreen = currentScreen,
                                language = currentLanguage,
                                onNavigate = { viewModel.navigateTo(it) }
                            )
                        }
                    },
                    snackbarHost = { SnackbarHost(snackbarHostState) },
                    contentWindowInsets = WindowInsets.safeDrawing
                ) { innerPadding ->
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        AnimatedContent(
                            targetState = currentScreen,
                            transitionSpec = {
                                fadeIn() togetherWith fadeOut()
                            },
                            label = "screen_transition"
                        ) { targetScreen ->
                            when (targetScreen) {
                                Screen.SPLASH -> SplashScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onReady = { viewModel.navigateTo(Screen.ONBOARDING) }
                                )
                                Screen.ONBOARDING -> OnboardingScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onContinue = { viewModel.navigateTo(Screen.SIGN_IN) }
                                )
                                Screen.SIGN_IN -> SignInScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onSuccess = { viewModel.navigateTo(Screen.HOME) }
                                )
                                Screen.HOME -> HomeScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onNavigate = { viewModel.navigateTo(it) }
                                )
                                Screen.CHAT -> ChatScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onNavigateToEditor = { viewModel.navigateTo(Screen.EDITOR) }
                                )
                                Screen.WIZARD -> WizardScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onNavigateToEditor = { viewModel.navigateTo(Screen.EDITOR) }
                                )
                                Screen.EDITOR -> EditorScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onNavigateToPublish = { viewModel.navigateTo(Screen.PUBLISH) }
                                )
                                Screen.SHORTS -> ShortsScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onNavigateToPublish = { viewModel.navigateTo(Screen.PUBLISH) }
                                )
                                Screen.THUMBNAILS -> ThumbnailScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage
                                )
                                Screen.VOICE -> VoiceScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage
                                )
                                Screen.PROJECTS -> ProjectsScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onOpenProject = { viewModel.navigateTo(Screen.EDITOR) },
                                    onCreateNew = { viewModel.navigateTo(Screen.WIZARD) }
                                )
                                Screen.AUTOMATION -> AutomationScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage
                                )
                                Screen.CHANNEL -> ChannelScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage
                                )
                                Screen.PUBLISH -> PublishScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onPublished = { viewModel.navigateTo(Screen.CHANNEL) }
                                )
                                Screen.ANALYTICS -> AnalyticsScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage
                                )
                                Screen.SETTINGS -> SettingsScreen(
                                    viewModel = viewModel,
                                    language = currentLanguage,
                                    onSignOut = { viewModel.navigateTo(Screen.SIGN_IN) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
