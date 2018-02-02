package nuclear.com.bloggy.Entity.WebSocket

import com.google.gson.annotations.SerializedName

enum class MessageType(val index: Int) {
    NOTIFICATION(0),
    BE_FOLLOWED(1),
    MAIL_RECEIVED(2),
    COMMENT_ADDED(3),
    POST_ADDED(4),
}


data class WebSocketWrapper(val uuid: String,
                            @SerializedName("is_binary") val isBinary: Boolean,
                            @SerializedName("timestamp") val timeStamp: Long,
                            @SerializedName("msg_from") val msgFrom: Int,
                            @SerializedName("msg_type") val msgType: MessageType,
                            val payload: String)