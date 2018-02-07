package nuclear.com.bloggy.Network.WebSocketManager

import android.content.Context
import android.os.Handler
import android.os.Looper
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.NetworkUtil
import okhttp3.*
import okio.ByteString
import java.util.concurrent.locks.ReentrantLock

class WebSocketManager(builder: Builder) : IWebSocketManager {
    private val mHandler = Handler(Looper.getMainLooper())
    private val doReconnect = { managerListener?.onReconnect(); initConnect() }
    private val mLock = ReentrantLock()
    private val mContext: Context
    private val mUrl: String
    private val mOkHttpClient: OkHttpClient
    private val mRequest: Request
    private val mWebSocketListener = object : WebSocketListener() {
        override fun onOpen(webSocket: WebSocket, response: Response) {
            websocket = webSocket
            websocketStatus = WebSocketStatus.CONNECTED
            cancelReconnect()
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onOpen(response) })
            } else {
                managerListener?.onOpen(response)
            }
        }

        override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
            tryReconnect()
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onFailure(t, response) })
            } else {
                managerListener?.onFailure(t, response)
            }
        }

        override fun onClosing(webSocket: WebSocket, code: Int, reason: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onClosing(code, reason) })
            } else {
                managerListener?.onClosing(code, reason)
            }
        }

        override fun onMessage(webSocket: WebSocket, text: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onMessage(text) })
            } else {
                managerListener?.onMessage(text)
            }
        }

        override fun onMessage(webSocket: WebSocket, bytes: ByteString) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onBinaryMessage(bytes) })
            } else {
                managerListener?.onBinaryMessage(bytes)
            }
        }

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ managerListener?.onClosed(code, reason) })
            } else {
                managerListener?.onClosed(code, reason)
            }
        }
    }

    private var isAutoReconnect = true
    private var isManualClosed = false
    private var reconnectCount = 0

    var managerListener: WebSocketManagerListener? = null
    var websocketStatus = WebSocketStatus.DISCONNECTED
        @Synchronized
        private set
        @Synchronized
        get
    var websocket: WebSocket? = null
        private set

    companion object {
        private const val BASE_RECONNECT_INTERVAL = 1000
        private const val MAX_RECONNECT_INTERVAL = 128 * BASE_RECONNECT_INTERVAL
    }

    class Builder(internal val mContext: Context) {
        internal lateinit var mUrl: String
        internal var autoReconnect = true
        internal lateinit var mClient: OkHttpClient
        internal lateinit var mRequest: Request

        fun url(url: String): Builder {
            mUrl = url
            return this
        }

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

        fun build() = WebSocketManager(this)
    }

    init {
        mContext = builder.mContext
        mUrl = builder.mUrl
        isAutoReconnect = builder.autoReconnect
        mOkHttpClient = builder.mClient
        mRequest = builder.mRequest
    }

    @Synchronized
    private fun initConnect() {
        if (!NetworkUtil.isConnected(mContext)) {
            websocketStatus = WebSocketStatus.DISCONNECTED
            websocket = null
            LogUtil.w(this, "WebSocket connect failed: Network not available.")
            return
        }
        if (websocketStatus == WebSocketStatus.CONNECTING || websocketStatus == WebSocketStatus.CONNECTED) {
            LogUtil.w(this, "WebSocket is ${websocketStatus.name}")
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
        if (!isAutoReconnect || isManualClosed)
            return
        if (!NetworkUtil.isConnected(mContext)) {
            websocketStatus = WebSocketStatus.DISCONNECTED
            websocket = null
            return
        }
        websocketStatus = WebSocketStatus.RECONNECTING
        val delay = (1 shl reconnectCount) * BASE_RECONNECT_INTERVAL.toLong()
        if (delay > MAX_RECONNECT_INTERVAL) {
            cancelReconnect()
            isManualClosed = true
            LogUtil.e(this, "reach max reconnect interval, reconnect failed, count:$reconnectCount")
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
        mOkHttpClient.dispatcher().cancelAll()
        if (websocket != null) {
            val isNormalClosed = websocket!!.close(WebSocketCode.NORMAL_CLOSE.index, WebSocketCode.NORMAL_CLOSE.name)
            if (!isNormalClosed) {
                managerListener?.onClosed(WebSocketCode.ABNORMAL_CLOSE.index, WebSocketCode.ABNORMAL_CLOSE.name)
            }
        }
        websocketStatus = WebSocketStatus.DISCONNECTED
        websocket = null
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