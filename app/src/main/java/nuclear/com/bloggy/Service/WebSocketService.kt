package nuclear.com.bloggy.Service

import android.app.Service
import android.content.Intent
import android.os.IBinder
import nuclear.com.bloggy.Network.WebSocketManager.WebSocketManager
import nuclear.com.bloggy.Network.WebSocketManager.IWebSocketManagerListener
import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.Util.OkHttpUtil
import okhttp3.Request
import okhttp3.Response
import okio.ByteString

class WebSocketService : Service() {
    private lateinit var mWebSocketManager: WebSocketManager

    override fun onCreate() {
        val request = Request.Builder()
                .url(Settings.INSTANCE.WebSocketUrl)
                .addHeader("Authorization", OkHttpUtil.genAuthHeader(Settings.INSTANCE.AuthToken!!))
                .build()
        mWebSocketManager = WebSocketManager.Builder(this)
                .autoReconnect(true)
                .client(OkHttpUtil.genOkHttpClient(OkHttpUtil.INTERCEPTOR_LOGGING))
                .request(request)
                .build()!!
        mWebSocketManager.listener = object : IWebSocketManagerListener {
            override fun onOpen(response: Response) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onMessage(message: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onBinaryMessage(byteString: ByteString) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onReconnect() {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onClosing(code: Int, reason: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onClosed(code: Int, reason: String) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }

            override fun onFailure(throwable: Throwable, response: Response?) {
                TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
            }
        }
        super.onCreate()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        mWebSocketManager.connect()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onDestroy() {
        mWebSocketManager.disConnect()
        super.onDestroy()
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

}
