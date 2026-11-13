package com.ccnaacademy.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ccnaacademy.app.ui.nav.AppRoot
import com.ccnaacademy.app.ui.theme.CcnaTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val progress by vm.progress.collectAsStateWithLifecycle()
            val dark = when (progress.themeMode) { "light" -> false; "dark" -> true; else -> isSystemInDarkTheme() }
            CcnaTheme(darkTheme = dark, dynamicColor = progress.dynamicColor) { AppRoot(vm) }
        }
    }
}
