package com.dom.kodiremote.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.tooling.preview.devices.WearDevices
import com.dom.kodiremote.kodi.NetworkKodiClient
import com.dom.kodiremote.presentation.theme.KodiRemoteTheme
import kotlinx.coroutines.launch
import android.util.Log
import androidx.lifecycle.lifecycleScope
import androidx.wear.tiles.TileService
import com.dom.kodiremote.tile.MainTileService

import android.content.Intent

class MainActivity : ComponentActivity() {
    companion object {
        const val EXTRA_TILE_ACTION = "tile_action"

        const val TILE_ACTION_PLAY_PAUSE = "play_pause"
        const val TILE_ACTION_STOP = "stop"
        const val TILE_ACTION_NEXT = "next"
        const val TILE_ACTION_PREVIOUS = "previous"

        const val SEEK_OFFSET_SMALL_SECONDS = 10
        const val SEEK_OFFSET_LARGE_SECONDS = 30

        const val TILE_ACTION_SEEK_BACK_SMALL = "seek_back_small"
        const val TILE_ACTION_SEEK_BACK_LARGE = "seek_back_large"
        const val TILE_ACTION_SEEK_FORWARD_SMALL = "seek_forward_small"
        const val TILE_ACTION_SEEK_FORWARD_LARGE = "seek_forward_large"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()

        super.onCreate(savedInstanceState)

        setTheme(android.R.style.Theme_DeviceDefault)

        val tileAction = intent?.getStringExtra(EXTRA_TILE_ACTION)
        val isLaunchedFromHistory = intent.flags and Intent.FLAG_ACTIVITY_LAUNCHED_FROM_HISTORY != 0
        
        if (tileAction != null && !isLaunchedFromHistory) {
            lifecycleScope.launch {
                try {
                    val client = NetworkKodiClient()
                    when (tileAction) {
                        TILE_ACTION_PLAY_PAUSE -> client.playPause()
                        TILE_ACTION_STOP -> client.stop()
                        TILE_ACTION_NEXT -> client.next()
                        TILE_ACTION_PREVIOUS -> client.previous()
                        TILE_ACTION_SEEK_BACK_LARGE -> client.seekBy(-SEEK_OFFSET_LARGE_SECONDS)
                        TILE_ACTION_SEEK_BACK_SMALL -> client.seekBy(-SEEK_OFFSET_SMALL_SECONDS)
                        TILE_ACTION_SEEK_FORWARD_SMALL -> client.seekBy(SEEK_OFFSET_SMALL_SECONDS)
                        TILE_ACTION_SEEK_FORWARD_LARGE -> client.seekBy(SEEK_OFFSET_LARGE_SECONDS)
                        else -> Log.w("KodiRemote", "Unknown tile action: $tileAction")
                    }
                } catch (e: Exception) {
                    Log.e("KodiRemote", "Tile action failed: $tileAction", e)
                } finally {
                    TileService.getUpdater(this@MainActivity)
                        .requestUpdate(MainTileService::class.java)
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

    KodiRemoteTheme {
        var isPlaying by remember { mutableStateOf(false) }

        suspend fun refreshIsPlaying() {
            try {
                isPlaying = client.isPlaying()
            } catch (e: Exception) {
                Log.e("KodiRemote", "Error in isPlaying", e)
            }
        }

        LaunchedEffect(Unit) {
            refreshIsPlaying()
        }

        val controlButtonSize = 42.12.dp
        val contentTopOffsetDp = 16.85.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
        ) {
            Spacer(modifier = Modifier.height(contentTopOffsetDp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            onClick = {
                            scope.launch {
                                try {
                                    client.playPause()
                                    refreshIsPlaying()
                                } catch (e: Exception) {
                                    Log.e("KodiRemote", "Error in playPause", e)
                                }
                            }
                        })
                        {
                            Icon(
                                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (isPlaying) "Pause" else "Play"
                            )
                        }

                        Spacer(modifier = Modifier.width(2.dp))

                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                            scope.launch {
                                try {
                                    client.stop()
                                    isPlaying = false
                                    refreshIsPlaying()
                                } catch (e: Exception) {
                                    Log.e("KodiRemote", "Error in stop", e)
                                }
                            }
                        })
                        {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = "Stop")
                        }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                                scope.launch {
                                    try {
                                        client.seekBy(-MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                                        refreshIsPlaying()
                                    } catch (e: Exception) {
                                        Log.e("KodiRemote", "Error in seekBy(-30)", e)
                                    }
                                }
                            }
                        ) {
                            Text("-${MainActivity.SEEK_OFFSET_LARGE_SECONDS}", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                                scope.launch {
                                    try {
                                        client.seekBy(-MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                                        refreshIsPlaying()
                                    } catch (e: Exception) {
                                        Log.e("KodiRemote", "Error in seekBy(-10)", e)
                                    }
                                }
                            }
                        ) {
                            Text("-${MainActivity.SEEK_OFFSET_SMALL_SECONDS}", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                                scope.launch {
                                    try {
                                        client.seekBy(MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                                        refreshIsPlaying()
                                    } catch (e: Exception) {
                                        Log.e("KodiRemote", "Error in seekBy(10)", e)
                                    }
                                }
                            }
                        ) {
                            Text("+${MainActivity.SEEK_OFFSET_SMALL_SECONDS}", fontWeight = FontWeight.Bold)
                        }

                        Button(
                            modifier = Modifier.size(controlButtonSize),
                            colors = ButtonDefaults.secondaryButtonColors(),
                            onClick = {
                                scope.launch {
                                    try {
                                        client.seekBy(MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                                        refreshIsPlaying()
                                    } catch (e: Exception) {
                                        Log.e("KodiRemote", "Error in seekBy(30)", e)
                                    }
                                }
                            }
                        ) {
                            Text("+${MainActivity.SEEK_OFFSET_LARGE_SECONDS}", fontWeight = FontWeight.Bold)
                        }
            }

            Spacer(modifier = Modifier.height(2.dp))

            Row(
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    modifier = Modifier.size(controlButtonSize),
                    onClick = {
                        scope.launch {
                            try {
                                client.previous()
                                refreshIsPlaying()
                            } catch (e: Exception) {
                                Log.e("KodiRemote", "Error in previous", e)
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.SkipPrevious, contentDescription = "Previous")
                }

                Spacer(modifier = Modifier.width(4.dp))

                Button(
                    modifier = Modifier.size(controlButtonSize),
                    onClick = {
                        scope.launch {
                            try {
                                client.next()
                                refreshIsPlaying()
                            } catch (e: Exception) {
                                Log.e("KodiRemote", "Error in next", e)
                            }
                        }
                    }
                ) {
                    Icon(imageVector = Icons.Default.SkipNext, contentDescription = "Next")
                }
            }
        }
    }
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}
