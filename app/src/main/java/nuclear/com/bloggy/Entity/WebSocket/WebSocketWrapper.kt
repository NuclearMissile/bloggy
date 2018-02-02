package nuclear.com.bloggy.Entity.WebSocket

import com.google.gson.annotations.SerializedName

enum class MessageType(val index: Int) {
    NOTIFICATION(0),
    BE_FOLLOWED(1),
    MAIL_RECEIVED(2),
    NEW_COMMENT(3),
    NEW_POST(4),
    COMMENT_BE_DELETED(5),
    POST_BE_DELETED(6),
}


data class WebSocketWrapper<out T>(val uuid: String,
                                   @SerializedName("is_binary") val isBinary: Boolean,
                                   @SerializedName("timestamp") val timeStamp: Long,
                                   @SerializedName("msg_from") val msgFrom: Int,
                                   @SerializedName("msg_type") val msgType: MessageType,
                                   val payload: T)