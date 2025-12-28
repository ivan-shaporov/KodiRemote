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
