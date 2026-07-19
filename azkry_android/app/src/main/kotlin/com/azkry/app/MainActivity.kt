package com.azkry.app

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.azkry.app.app.AzkryRoot
import com.azkry.app.app.NotificationDestination
import com.azkry.app.features.notifications.services.ReminderScheduler
import com.azkry.app.features.prayertimes.services.PrayerLocationRefresher
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var reminderScheduler: ReminderScheduler

    @Inject
    lateinit var prayerLocationRefresher: PrayerLocationRefresher

    private val pendingDestination = MutableStateFlow<NotificationDestination?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        applyEdgeToEdge(darkTheme = true)
        super.onCreate(savedInstanceState)
        handleNotificationIntent(intent)

        // Keep the reminder chain alive and the location fresh: prayer times
        // drift day to day, so every app open re-plans from current data.
        lifecycleScope.launch {
            prayerLocationRefresher.refreshIfAutoLocating()
            reminderScheduler.rescheduleNext()
        }

        setContent {
            val destination = pendingDestination.collectAsStateWithLifecycle()
            AzkryRoot(
                pendingDestination = destination.value,
                onDestinationConsumed = ::clearDestination,
                onDarkThemeChanged = ::applyEdgeToEdge,
            )
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        handleNotificationIntent(intent)
    }

    private fun applyEdgeToEdge(darkTheme: Boolean) {
        val systemBarStyle = if (darkTheme) {
            SystemBarStyle.dark(Color.TRANSPARENT)
        } else {
            SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT)
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )
    }

    private fun handleNotificationIntent(intent: Intent?) {
        NotificationDestination.fromIntent(intent)?.let { destination ->
            pendingDestination.value = destination
        }
    }

    private fun clearDestination(destination: NotificationDestination) {
        if (pendingDestination.value == destination) {
            pendingDestination.value = null
        }
    }
}
