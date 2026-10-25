package com.parkin.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.google.android.gms.maps.MapsInitializer
import com.parkin.app.ui.theme.ParkInTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        try {
            MapsInitializer.initialize(applicationContext, MapsInitializer.Renderer.LATEST) {

            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        setContent {
            var isDarkTheme by remember {
                mutableStateOf(false)
            }

            ParkInTheme(darkTheme = isDarkTheme) {
                AppNavigation(
                    isDarkTheme = isDarkTheme,
                    onThemeChange = {
                        isDarkTheme = it
                    }
                )
            }
        }
    }
}