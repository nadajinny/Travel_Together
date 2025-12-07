package com.example.myapplication.data.model

import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import java.net.URISyntaxException

object SocketManager {
    private const val SERVER_URL = "http://192.168.108.12:3005"
    private var socket: Socket? = null

    fun initializeSocket() {
        if (socket == null) {
            try {
                socket = IO.socket(SERVER_URL)
            } catch (e: URISyntaxException) {
                Log.e("SocketManager", "Socket Initialization Error: ${e.message}")
            }
        }
    }

    fun connect() {
        socket?.connect()
    }

    fun disconnect() {
        socket?.disconnect()
    }

    fun on(event: String, listener: Emitter.Listener) {
        socket?.on(event, listener)
    }

    fun off(event: String, listener: Emitter.Listener) {
        socket?.off(event, listener)
    }

    fun emit(event: String, data: Any) {
        socket?.emit(event, data)
    }

    fun isConnected(): Boolean {
        return socket?.connected() == true
    }
}
