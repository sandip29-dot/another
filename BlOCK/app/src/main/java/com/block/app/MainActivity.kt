package com.block.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.block.app.data.settings.SettingsDataStore
import com.block.app.ui.navigation.BlockNavGraph
import com.block.app.ui.screens.onboarding.OnboardingScreen
import com.block.app.ui.theme.BlockColors
import com.block.app.ui.theme.BlockTheme
import com.block.app.watchdog.ServiceMonitor
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Main launcher activity for BlOCK.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var settingsDataStore: SettingsDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Schedule periodic watchdog worker on app start
        ServiceMonitor.scheduleWatchdog(this)

        setContent {
            BlockTheme {
                val onboardingComplete by settingsDataStore.onboardingComplete
                    .collectAsStateWithLifecycle(initialValue = false)

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = BlockColors.Black
                ) {
                    if (onboardingComplete) {
                        BlockNavGraph()
                    } else {
                        OnboardingScreen(
                            onComplete = {
                                // Transition to main app automatically handled by state
                            }
                        )
                    }
                }
            }
        }
    }
}
