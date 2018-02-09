package nuclear.com.bloggy.Network.WebSocketManager

import android.content.Context
import android.os.Handler
import android.os.Looper
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.NetworkUtil
import okhttp3.*
import okio.ByteString
import java.util.concurrent.locks.ReentrantLock

class WebSocketManager private constructor(builder: Builder) : IWebSocketManager {
    private val mHandler = Handler(Looper.getMainLooper())
    private val doReconnect = {
        LogUtil.i(this, "do reconnect, times: $reconnectCount")
        listener?.onReconnect()
        initConnect()
    }
    private val mLock = ReentrantLock()
    private val mWebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            websocket = webSocket
            websocketStatus = WebSocketStatus.CONNECTED
            cancelReconnect()
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onOpen(response) })
            } else {
                listener?.onOpen(response)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onFailure(t, response) })
            } else {
                listener?.onFailure(t, response)
            }
            tryReconnect()
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onClosing(code, reason) })
            } else {
                listener?.onClosing(code, reason)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onMessage(text) })
            } else {
                listener?.onMessage(text)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onBinaryMessage(bytes) })
            } else {
                listener?.onBinaryMessage(bytes)
            }
        }

        // only invoke while websocket normal closed
        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onClosed(code, reason) })
            } else {
                listener?.onClosed(code, reason)
            }
            mOkHttpClient.dispatcher().cancelAll()
            websocketStatus = WebSocketStatus.DISCONNECTED
            websocket = null
        }
    }
    private var mContext: Context
    private var mOkHttpClient: OkHttpClient
    private var mRequest: Request

    var listener: IWebSocketManagerListener? = null
    var websocketStatus = WebSocketStatus.DISCONNECTED
        @Synchronized
        private set(value) {
            if (field != value) {
                listener?.onStatusChanged(field, value)
                field = value
            }
        }
    var websocket: WebSocket? = null
        private set
    var isAutoReconnect: Boolean = true
        private set
    var isManualClosed = false
        private set
    var reconnectCount = 0
        private set

    companion object {
        private const val BASE_RECONNECT_INTERVAL = 1000
        private const val MAX_RECONNECT_COUNT = 5
    }

    class Builder(val context: Context) {
        var autoReconnect = true
            private set
        lateinit var mClient: OkHttpClient
            private set
        lateinit var mRequest: Request
            private set
        val mContext: Context = context

        fun request(request: Request): Builder {
            mRequest = request
            return this
        }

        fun client(client: OkHttpClient): Builder {
            mClient = client
            return this
        }

        fun autoReconnect(flag: Boolean): Builder {
            autoReconnect = flag
            return this
        }

        fun build(): WebSocketManager? {
            try {
                return WebSocketManager(this)
            } catch (e: UninitializedPropertyAccessException) {
                e.printStackTrace()
            }
            return null
        }
    }

    init {
        mContext = builder.mContext
        mRequest = builder.mRequest
        isAutoReconnect = builder.autoReconnect
        mOkHttpClient = builder.mClient
    }

    @Synchronized
    private fun initConnect() {
        if (websocketStatus == WebSocketStatus.CONNECTING || websocketStatus == WebSocketStatus.CONNECTED) {
            LogUtil.w(this, "WebSocket is ${websocketStatus.name}")
            return
        }
        if (!NetworkUtil.isConnected(mContext)) {
            disConnect()
            LogUtil.w(this, "WebSocket connect failed: Network not available.")
            return
        }
        websocketStatus = WebSocketStatus.CONNECTING
        mOkHttpClient.dispatcher().cancelAll()
        try {
            mLock.lockInterruptibly()
            try {
                mOkHttpClient.newWebSocket(mRequest, mWebSocketListener)
            } finally {
                mLock.unlock()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun tryReconnect() {
        if (!isAutoReconnect || isManualClosed) {
            disConnect()
            return
        }
        if (!NetworkUtil.isConnected(mContext)) {
            disConnect()
            return
        }
        websocketStatus = WebSocketStatus.RECONNECTING
        val delay = (1 shl reconnectCount) * BASE_RECONNECT_INTERVAL.toLong()
        if (reconnectCount >= MAX_RECONNECT_COUNT) {
            LogUtil.w(this, "reach max reconnect interval, reconnect failed, count:$reconnectCount")
            disConnect()
            return
        }
        mHandler.postDelayed(doReconnect, delay)
        reconnectCount++
    }

    private fun cancelReconnect() {
        mHandler.removeCallbacks(doReconnect)
        reconnectCount = 0
    }

    override fun connect() {
        isManualClosed = false
        initConnect()
    }

    override fun disConnect() {
        if (websocketStatus == WebSocketStatus.DISCONNECTED || websocket == null)
            return
        isManualClosed = true
        cancelReconnect()
        val isNormalClosed = websocket?.close(WebSocketCode.NORMAL_CLOSE.index, WebSocketCode.NORMAL_CLOSE.toString())
                ?: false
        if (!isNormalClosed) {
            listener?.onClosed(WebSocketCode.ABNORMAL_CLOSE.index, WebSocketCode.ABNORMAL_CLOSE.toString())
            mOkHttpClient.dispatcher().cancelAll()
            websocketStatus = WebSocketStatus.DISCONNECTED
            websocket = null
        }
    }

    override fun getWebSocket(): WebSocket? = websocket

    override fun getStatus(): WebSocketStatus = websocketStatus

    override fun sendMessage(msg: String): Boolean {
        var isSend = false
        if (websocket != null && websocketStatus == WebSocketStatus.CONNECTED) {
            isSend = websocket!!.send(msg)
            if (!isSend)
                tryReconnect()
        }
        return isSend
    }

    override fun sendBinaryMessage(byteString: ByteString): Boolean {
        var isSend = false
        if (websocket != null && websocketStatus == WebSocketStatus.CONNECTED) {
            isSend = websocket!!.send(byteString)
            if (!isSend)
                tryReconnect()
        }
        return isSend
    }
}