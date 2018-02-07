package nuclear.com.bloggy.Network.WebSocketManager

import okhttp3.Response
import okio.ByteString

abstract class WebSocketManagerListener {
    open fun onOpen(response: Response) {}

    open fun onMessage(message: String) {}

    open fun onBinaryMessage(byteString: ByteString) {}

    open fun onReconnect() {}

    open fun onClosing(code: Int, reason: String) {}

    open fun onClosed(code: Int, reason: String) {}

    open fun onFailure(throwable: Throwable, response: Response?) {}
}

fun genListener(onOpen: ((response: Response) -> Unit)? = null,
                onMessage: ((message: String) -> Unit)? = null,
                onBinaryMessage: ((byteString: ByteString) -> Unit)? = null,
                onReconnect: (() -> Unit)? = null,
                onClosing: ((code: Int, reason: String) -> Unit)? = null,
                onClosed: ((code: Int, reason: String) -> Unit)? = null,
                onFailure: ((throwable: Throwable, response: Response?) -> Unit)? = null) = object : WebSocketManagerListener() {
    override fun onOpen(response: Response) {
        onOpen?.invoke(response)
    }

    override fun onMessage(message: String) {
        onMessage?.invoke(message)
    }

    override fun onBinaryMessage(byteString: ByteString) {
        onBinaryMessage?.invoke(byteString)
    }

    override fun onReconnect() {
        onReconnect?.invoke()
    }

    override fun onClosing(code: Int, reason: String) {
        onClosing?.invoke(code, reason)
    }

    override fun onClosed(code: Int, reason: String) {
        onClosed?.invoke(code, reason)
    }

    override fun onFailure(throwable: Throwable, response: Response?) {
        onFailure?.invoke(throwable, response)
    }
}