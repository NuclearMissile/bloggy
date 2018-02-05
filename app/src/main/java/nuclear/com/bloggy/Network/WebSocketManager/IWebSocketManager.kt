package nuclear.com.bloggy.Network.WebSocketManager

import okhttp3.WebSocket
import okio.ByteString

interface IWebSocketManager {
    fun getWebSocket(): WebSocket?

    fun connect()

    fun disConnect()

    fun tryReconnect()

    fun cancelReconnect()

    fun isConnected(): Boolean

    fun getStatus(): WebSocketStatus

    // fun setWebsocketStatus(websocketStatus: WebSocketStatus)

    fun sendMessage(msg: String): Boolean

    fun sendBinaryMessage(byteString: ByteString): Boolean
}