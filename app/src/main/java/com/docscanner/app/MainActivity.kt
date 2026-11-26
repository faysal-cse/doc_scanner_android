package com.docscanner.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.navigation.compose.rememberNavController
import com.docscanner.app.ui.navigation.AppNavHost
import com.docscanner.app.ui.theme.AppearanceMode
import com.docscanner.app.ui.theme.DocScannerTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val prefs = getSharedPreferences("doc_scanner_prefs", Context.MODE_PRIVATE)
        val app = application as DocScannerApplication

        setContent {
            var appearanceMode by remember {
                mutableStateOf(
                    when (prefs.getInt("appearance_mode", 0)) {
                        1 -> AppearanceMode.LIGHT
                        2 -> AppearanceMode.DARK
                        else -> AppearanceMode.AUTO
                    }
                )
            }

            var onboardingComplete by remember {
                mutableStateOf(prefs.getBoolean("onboarding_complete", false))
            }

            val navController = rememberNavController()

            DocScannerTheme(appearanceMode = appearanceMode) {
                AppNavHost(
                    navController = navController,
                    documentStore = app.documentStore,
                    onboardingComplete = onboardingComplete,
                    onOnboardingComplete = {
                        onboardingComplete = true
                        prefs.edit().putBoolean("onboarding_complete", true).apply()
                    }
                )
            }
        }
    }
}
