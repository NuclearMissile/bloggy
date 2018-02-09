package nuclear.com.bloggy.BroadcastReceiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import nuclear.com.bloggy.BaseApplication
import nuclear.com.bloggy.Entity.WebSocket.BeFollowedMessage
import nuclear.com.bloggy.Entity.WebSocket.CommentAddedMessage
import nuclear.com.bloggy.Entity.WebSocket.MessageType
import nuclear.com.bloggy.Entity.WebSocket.PostAddedMessage
import nuclear.com.bloggy.Settings
import nuclear.com.bloggy.UI.Activity.PostActivity
import nuclear.com.bloggy.UI.Activity.UserInfoActivity
import nuclear.com.bloggy.Util.ToastUtil

class NotificationClickReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val type = intent.getIntExtra("msg_type", Int.MIN_VALUE)
        val m: Parcelable? = intent.getParcelableExtra("msg")
        when (type) {
            -1 -> BaseApplication.openFromBackground(context)
            MessageType.COMMENT_ADDED.index -> {
                val msg = m!! as CommentAddedMessage
                PostActivity.tryStart(context, msg.postId, 1)
            }
            MessageType.POST_ADDED.index -> {
                val msg = m!! as PostAddedMessage
                PostActivity.tryStart(context, msg.postId)
            }
            MessageType.BE_FOLLOWED.index -> {
                val msg = m!! as BeFollowedMessage
                UserInfoActivity.tryStart(context, msg.followerId)
            }
            Int.MIN_VALUE -> {
                if (Settings.INSTANCE.DebugMode) {
                    ToastUtil.showLongToast("Unknown msg type")
                }
            }
        }
    }
}
