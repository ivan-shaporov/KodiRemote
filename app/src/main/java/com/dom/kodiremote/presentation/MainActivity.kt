package com.dom.kodiremote.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.tooling.preview.devices.WearDevices
import com.dom.kodiremote.kodi.NetworkKodiClient
import com.dom.kodiremote.presentation.theme.KodiRemoteTheme
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_TILE_ACTION = "tile_action"

        const val TILE_ACTION_PLAY_PAUSE = "play_pause"
        const val TILE_ACTION_STOP = "stop"
        const val TILE_ACTION_NEXT = "next"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val tileAction = intent?.getStringExtra(EXTRA_TILE_ACTION)
        if (tileAction != null) {
            lifecycleScope.launch {
                try {
                    val client = NetworkKodiClient()
                    when (tileAction) {
                        TILE_ACTION_PLAY_PAUSE -> client.playPause()
                        TILE_ACTION_STOP -> client.stop()
                        TILE_ACTION_NEXT -> client.next()
                        else -> Log.w("KodiRemote", "Unknown tile action: $tileAction")
                    }
                } catch (e: Exception) {
                    Log.e("KodiRemote", "Tile action failed: $tileAction", e)
                } finally {
                    finish()
                }
            }
            return
        }

        setContent {
            WearApp()
        }
    }
}

@Composable
fun WearApp() {
    val client = remember { NetworkKodiClient() }
    val scope = rememberCoroutineScope()
    val listState = rememberScalingLazyListState()

    KodiRemoteTheme {
        var paused by remember { mutableStateOf(true) }

        ScalingLazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            state = listState,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            item {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Button(onClick = {
                        scope.launch {
                            try {
                                client.playPause()
                                paused = !paused
                            } catch (e: Exception) {
                                Log.e("KodiRemote", "Error in playPause", e)
                            }
                        }
                    }) {
                        Icon(
                            imageVector = if (paused) Icons.Default.PlayArrow else Icons.Default.Pause,
                            contentDescription = if (paused) "Play" else "Pause"
                        )
                    }
                    Button(onClick = {
                        scope.launch {
                            try {
                                client.stop()
                                paused = true
                            } catch (e: Exception) {
                                Log.e("KodiRemote", "Error in stop", e)
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                    }
                    Button(onClick = {
                        scope.launch {
                            try {
                                client.next()
                            } catch (e: Exception) {
                                Log.e("KodiRemote", "Error in next", e)
                            }
                        }
                    }) {
                        Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
                    }
                }
            }

            item { Spacer(modifier = Modifier.height(8.dp)) }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}
