package com.dom.kodiremote.kodi

interface KodiClient {
    fun playPause()
    fun next()
    fun stop()
    fun isPlaying(): Boolean
}