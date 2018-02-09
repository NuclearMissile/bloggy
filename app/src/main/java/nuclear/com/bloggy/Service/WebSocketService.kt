package nuclear.com.bloggy.Service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.app.NotificationCompat
import com.google.gson.Gson
import nuclear.com.bloggy.BroadcastReceiver.NotificationClickReceiver
import nuclear.com.bloggy.Entity.WebSocket.*
import nuclear.com.bloggy.Network.WebSocketManager.WebSocketManager
import nuclear.com.bloggy.Network.WebSocketManager.getListener
import nuclear.com.bloggy.R
import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.UserHolder
import nuclear.com.bloggy.Util.LogUtil
import nuclear.com.bloggy.Util.OkHttpUtil
import nuclear.com.bloggy.Util.ToastUtil
import okhttp3.Request
import okhttp3.Response

class WebSocketService : Service() {
    private lateinit var mWebSocketManager: WebSocketManager
    private lateinit var mNotificationManager: NotificationManager

    override fun onCreate() {
        val request = Request.Builder().url(Settings.INSTANCE.WebSocketUrl).build()
        mWebSocketManager = WebSocketManager.Builder(this)
                .autoReconnect(true)
                .client(OkHttpUtil.genOkHttpClient(OkHttpUtil.INTERCEPTOR_LOGGING,
                        OkHttpUtil.getAuthInterceptor(OkHttpUtil.genAuthHeader(Settings.INSTANCE.AuthToken!!))))
                .request(request)
                .build()!!
        setListener()
        mNotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= 26)
            mNotificationManager.createNotificationChannel(NotificationChannel("1", "Bloggy Service", NotificationManager.IMPORTANCE_DEFAULT))
        super.onCreate()
    }

    private fun setListener() {
        mWebSocketManager.listener = getListener(onOpen = {
            val clickIntent = Intent(this, NotificationClickReceiver::class.java)
            clickIntent.putExtra("msg_type", -1)
            val builder = NotificationCompat.Builder(this, "1")
                    .setContentTitle("Client ${UserHolder.currUser!!.username} is online.")
                    .setContentText("WebSocket service is running.")
                    .setSmallIcon(R.drawable.logo_small)
                    .setOngoing(true)
                    .setContentIntent(PendingIntent.getBroadcast(this, 0, clickIntent,
                            PendingIntent.FLAG_UPDATE_CURRENT))
            startForeground(1, builder.build())
            /*Glide.with(this)
                .asBitmap()
                .load(UserHolder.getAvatarUrl(48)!!)
                .apply(GlideOptions.DEF_OPTION)
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(resource: Bitmap, transition: Transition<in Bitmap>?) {
                        startForeground(1, builder.setLargeIcon(resource).build())
                    }
                })*/
        }, onClosed = { code, reason ->
            stopSelf()
        }, onFailure = { throwable: Throwable, response: Response? ->
            throwable.printStackTrace()
            if (Settings.INSTANCE.DebugMode)
                ToastUtil.showLongToast(throwable.message)
        }, onMessage = {
            val wrapper: WebSocketWrapper = Gson().fromJson(it, WebSocketWrapper::class.java)
            when (wrapper.msgType) {
                MessageType.BE_FOLLOWED -> {
                    val msg: BeFollowedMessage = Gson().fromJson(wrapper.payload, BeFollowedMessage::class.java)
                    val clickIntent = Intent(this, NotificationClickReceiver::class.java)
                    clickIntent.putExtra("msg_type", wrapper.msgType.index)
                    clickIntent.putExtra("msg", msg)
                    val builder = NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.logo_small)
                            .setContentTitle("Bloggy: Be followed.")
                            .setContentText(msg.msg)
                            .setContentIntent(PendingIntent.getBroadcast(this, 0,
                                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .setAutoCancel(true)
                    mNotificationManager.notify(2, builder.build())
                }
                MessageType.POST_ADDED -> {
                    val msg: PostAddedMessage = Gson().fromJson(wrapper.payload, PostAddedMessage::class.java)
                    val clickIntent = Intent(this, NotificationClickReceiver::class.java)
                    clickIntent.putExtra("msg_type", wrapper.msgType.index)
                    clickIntent.putExtra("msg", msg)
                    val builder = NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.logo_small)
                            .setContentTitle("Bloggy: New post.")
                            .setContentText(msg.msg)
                            .setContentIntent(PendingIntent.getBroadcast(this, 0,
                                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .setAutoCancel(true)
                    mNotificationManager.notify(3, builder.build())
                }
                MessageType.COMMENT_ADDED -> {
                    val msg: CommentAddedMessage = Gson().fromJson(wrapper.payload, CommentAddedMessage::class.java)
                    val clickIntent = Intent(this, NotificationClickReceiver::class.java)
                    clickIntent.putExtra("msg_type", wrapper.msgType.index)
                    clickIntent.putExtra("msg", msg)
                    val builder = NotificationCompat.Builder(this, "1")
                            .setSmallIcon(R.drawable.logo_small)
                            .setContentTitle("Bloggy: New comment to your post.")
                            .setContentText(msg.msg)
                            .setContentIntent(PendingIntent.getBroadcast(this, 0,
                                    clickIntent, PendingIntent.FLAG_UPDATE_CURRENT))
                            .setAutoCancel(true)
                    mNotificationManager.notify(4, builder.build())
                }
                else -> {
                    ToastUtil.showLongToast("Unknown message type")
                }
            }
        }, onStatusChanged = { oldStatus, newStatus ->
            LogUtil.i(this, "$oldStatus -> $newStatus")
        })
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
