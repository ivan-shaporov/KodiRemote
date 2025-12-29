package com.dom.kodiremote.kodi

import okhttp3.Credentials
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import com.dom.kodiremote.BuildConfig

class NetworkKodiClient(
    baseUrl: String = "http://${BuildConfig.KODI_HOST}:8080/"
) : KodiClient {

    private val service: KodiService

    init {
        val client = OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", Credentials.basic(BuildConfig.KODI_USERNAME, BuildConfig.KODI_PASSWORD))
                    .build()
                chain.proceed(request)
            }
            .build()

        val retrofit = Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        service = retrofit.create(KodiService::class.java)
    }

    override suspend fun playPause() {
        service.execute(
            RpcRequest(
                method = "Player.PlayPause",
                params = mapOf("playerid" to 1)
            )
        )
    }

    override suspend fun previous() {
        // 1. Get the current position in the playlist
        val response = service.execute(
            RpcRequest(
                method = "Player.GetProperties",
                params = mapOf("playerid" to 1, "properties" to listOf("position", "speed"))
            )
        )

        val result = response.result as? Map<*, *> ?: return
        val currentPosition = (result["position"] as? Number)?.toInt() ?: 0

        // 2. Only attempt if we aren't at the very first video (index 0)
        if (currentPosition > 0) {
            service.execute(
                RpcRequest(
                    method = "Player.Open",
                    params = mapOf(
                        "item" to mapOf("playlistid" to 1, "position" to currentPosition - 1)
                    )
                )
            )

        }
    }

    override suspend fun next() {
        service.execute(
            RpcRequest(
                method = "Player.GoTo",
                params = mapOf("playerid" to 1, "to" to "next")
            )
        )
    }

    override suspend fun stop() {
        service.execute(
            RpcRequest(
                method = "Player.Stop",
                params = mapOf("playerid" to 1)
            )
        )
    }

    override suspend fun seekBy(secondsDelta: Int) {
        fun numberToInt(value: Any?): Int? {
            return when (value) {
                is Int -> value
                is Long -> value.toInt()
                is Double -> value.toInt()
                is Float -> value.toInt()
                is Number -> value.toInt()
                else -> null
            }
        }

        suspend fun getActivePlayerId(): Int? {
            val response = service.execute(
                RpcRequest(
                    method = "Player.GetActivePlayers"
                )
            )
            val players = response.result as? List<*> ?: return null
            val first = players.firstOrNull() as? Map<*, *> ?: return null
            return numberToInt(first["playerid"])
        }

        fun timeMapToSeconds(time: Any?): Int? {
            val map = time as? Map<*, *> ?: return null
            val hours = numberToInt(map["hours"]) ?: 0
            val minutes = numberToInt(map["minutes"]) ?: 0
            val seconds = numberToInt(map["seconds"]) ?: 0
            return (hours * 3600) + (minutes * 60) + seconds
        }

        val playerId = getActivePlayerId() ?: 1

        val response = service.execute(
            RpcRequest(
                method = "Player.GetProperties",
                params = mapOf(
                    "playerid" to playerId,
                    "properties" to listOf("time", "totaltime")
                )
            )
        )

        val result = response.result as? Map<*, *> ?: return
        val currentSeconds = timeMapToSeconds(result["time"]) ?: return
        val totalSeconds = timeMapToSeconds(result["totaltime"])

        var targetSeconds = currentSeconds + secondsDelta
        if (targetSeconds < 0) targetSeconds = 0
        if (totalSeconds != null && totalSeconds > 0 && targetSeconds > totalSeconds) {
            targetSeconds = totalSeconds
        }

        val hours = targetSeconds / 3600
        val minutes = (targetSeconds % 3600) / 60
        val seconds = targetSeconds % 60

        val timeObject = mapOf(
            "hours" to hours,
            "minutes" to minutes,
            "seconds" to seconds,
            "milliseconds" to 0
        )

        // Kodi JSON-RPC accepts different "value" shapes across versions.
        // Try the more explicit nested form first; if it doesn't yield a result, fall back.
        val seekResponseNested = service.execute(
            RpcRequest(
                method = "Player.Seek",
                params = mapOf(
                    "playerid" to playerId,
                    "value" to mapOf("time" to timeObject)
                )
            )
        )

        if (seekResponseNested.result == null) {
            service.execute(
                RpcRequest(
                    method = "Player.Seek",
                    params = mapOf(
                        "playerid" to playerId,
                        "value" to timeObject
                    )
                )
            )
        }
    }

    override suspend fun isPlaying(): Boolean {
        try {
            val response = service.execute(
                RpcRequest(
                    method = "Player.GetProperties",
                    params = mapOf(
                        "playerid" to 1,
                        "properties" to listOf("speed")
                    )
                )
            )
            val result = response.result as? Map<*, *>
            val speed = result?.get("speed") as? Double
            return (speed ?: 0.0) != 0.0
        } catch (e: Exception) {
            return false
        }
    }
}
