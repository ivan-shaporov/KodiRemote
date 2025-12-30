package com.dom.kodiremote.service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.wear.tiles.TileService
import com.dom.kodiremote.kodi.NetworkKodiClient
import com.dom.kodiremote.presentation.MainActivity
import com.dom.kodiremote.tile.MainTileService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ActionService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val tileAction = intent?.getStringExtra(MainActivity.EXTRA_TILE_ACTION)
        
        if (tileAction != null) {
            scope.launch {
                try {
                    val client = NetworkKodiClient()
                    when (tileAction) {
                        MainActivity.TILE_ACTION_PLAY_PAUSE -> client.playPause()
                        MainActivity.TILE_ACTION_STOP -> client.stop()
                        MainActivity.TILE_ACTION_NEXT -> client.next()
                        MainActivity.TILE_ACTION_PREVIOUS -> client.previous()
                        MainActivity.TILE_ACTION_SEEK_BACK_LARGE -> client.seekBy(-MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                        MainActivity.TILE_ACTION_SEEK_BACK_SMALL -> client.seekBy(-MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                        MainActivity.TILE_ACTION_SEEK_FORWARD_SMALL -> client.seekBy(MainActivity.SEEK_OFFSET_SMALL_SECONDS)
                        MainActivity.TILE_ACTION_SEEK_FORWARD_LARGE -> client.seekBy(MainActivity.SEEK_OFFSET_LARGE_SECONDS)
                        else -> Log.w("KodiRemote", "Unknown action: $tileAction")
                    }
                } catch (e: Exception) {
                    Log.e("KodiRemote", "Action failed: $tileAction", e)
                } finally {
                    try {
                        TileService.getUpdater(applicationContext)
                            .requestUpdate(MainTileService::class.java)
                    } catch (e: Exception) {
                        Log.e("KodiRemote", "Failed to request tile update", e)
                    }
                    stopSelf(startId)
                }
            }
        } else {
            stopSelf(startId)
        }

        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}
