package com.dom.kodiremote.kodi

interface KodiClient {
    suspend fun playPause()
    suspend fun next()
    suspend fun stop()
    suspend fun seekBy(secondsDelta: Int)
    suspend fun isPlaying(): Boolean
}