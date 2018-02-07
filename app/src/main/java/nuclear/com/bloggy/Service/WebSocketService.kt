package nuclear.com.bloggy.Service

import android.app.Service
import android.content.Intent
import android.os.IBinder

class WebSocketService : Service() {
    override fun onBind(intent: Intent): IBinder? {
        throw UnsupportedOperationException("Not yet implemented")
    }
}
