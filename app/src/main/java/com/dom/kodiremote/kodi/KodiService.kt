package com.dom.kodiremote.kodi

import retrofit2.http.Body
import retrofit2.http.POST

data class RpcRequest(
    val jsonrpc: String = "2.0",
    val method: String,
    val params: Map<String, Any>? = null,
    val id: Int = 1
)

data class RpcResponse(
    val jsonrpc: String,
    val result: Any?,
    val id: Int
)

interface KodiService {
    @POST("jsonrpc")
    suspend fun execute(@Body request: RpcRequest): RpcResponse
}
