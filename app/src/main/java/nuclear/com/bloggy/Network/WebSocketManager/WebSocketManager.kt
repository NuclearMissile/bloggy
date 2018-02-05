package nuclear.com.bloggy.Network.WebSocketManager

import android.content.Context
import android.os.Handler
import android.os.Looper
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.NetworkUtil
import okhttp3.*
import okio.ByteString
import java.util.concurrent.locks.Lock
import java.util.concurrent.locks.ReentrantLock

class WebSocketManager(builder: Builder) : IWebSocketManager {
    private val mHandler = Handler(Looper.getMainLooper())
    private val doReconnect = { listener?.onReconnect(); initConnect() }
    private val mLock: Lock = ReentrantLock()
    private val mContext: Context
    private val mUrl: String
    private val mOkHttpClient: OkHttpClient
    private val mRequest: Request
    private val mWebSocketListener: WebSocketListener = object : WebSocketListener() {
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
            tryReconnect()
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onFailure(t, response) })
            } else {
                listener?.onFailure(t, response)
            }
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

        override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
            websocketStatus = WebSocketStatus.DISCONNECTED
            websocket = null
            if (Looper.myLooper() != Looper.getMainLooper()) {
                mHandler.post({ listener?.onClosed(code, reason) })
            } else {
                listener?.onClosed(code, reason)
            }
        }
    }

    private var isAutoReconnect = true
    private var isManualClosed = false
    private var reconnectCount = 0

    var listener: MyWebSocketListener? = null
    var websocketStatus = WebSocketStatus.DISCONNECTED
        @Synchronized
        private set
    var websocket: WebSocket? = null
        private set

    companion object {
        private const val BASE_RECONNECT_INTERVAL = 2000
        private const val MAX_RECONNECT_INTERVAL = 64 * BASE_RECONNECT_INTERVAL
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

        fun build(): WebSocketManager {
            return WebSocketManager(this)
        }
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

    override fun tryReconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun cancelReconnect() {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun connect() {
        isManualClosed = false
        initConnect()
    }

    override fun disConnect() {
        isManualClosed = true

    }

    override fun isConnected(): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getWebSocket(): WebSocket? = websocket

    override fun getStatus(): WebSocketStatus = websocketStatus

    override fun sendMessage(msg: String): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun sendBinaryMessage(byteString: ByteString): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }
}