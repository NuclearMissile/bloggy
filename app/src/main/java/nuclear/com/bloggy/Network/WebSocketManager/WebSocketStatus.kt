package nuclear.com.bloggy.Network.WebSocketManager

/**
 * Created by torri on 2018/2/5.
 */
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
