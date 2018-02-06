package nuclear.com.bloggy.Network.WebSocketManager

import okhttp3.WebSocket
import okio.ByteString

interface IWebSocketManager {
    fun getWebSocket(): WebSocket?

    fun connect()

    fun disConnect()

    fun getStatus(): WebSocketStatus

    fun sendMessage(msg: String): Boolean

    fun sendBinaryMessage(byteString: ByteString): Boolean
}