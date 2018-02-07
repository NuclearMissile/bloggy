package nuclear.com.bloggy.Network.WebSocketManager

enum class WebSocketStatus(val index: Int) {
    CONNECTED(1),
    CONNECTING(0),
    RECONNECTING(2),
    DISCONNECTED(-1),
}

enum class WebSocketCode(val index: Int) {
    NORMAL_CLOSE(1000),
    ABNORMAL_CLOSE(1001),
}
