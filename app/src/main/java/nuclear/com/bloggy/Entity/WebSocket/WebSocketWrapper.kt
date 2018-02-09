package nuclear.com.bloggy.Entity.WebSocket

import com.google.gson.*
import com.google.gson.annotations.JsonAdapter
import com.google.gson.annotations.SerializedName
import java.lang.reflect.Type

enum class MessageType(val index: Int) {
    NOTIFICATION(0),
    BE_FOLLOWED(1),
    MAIL_RECEIVED(2),
    COMMENT_ADDED(3),
    POST_ADDED(4),
}

private fun fromInt(index: Int) = when (index) {
    0 -> MessageType.NOTIFICATION
    1 -> MessageType.BE_FOLLOWED
    2 -> MessageType.MAIL_RECEIVED
    3 -> MessageType.COMMENT_ADDED
    4 -> MessageType.POST_ADDED
    else -> throw IllegalArgumentException()
}


class WebSocketWrapperDeserializer : JsonDeserializer<WebSocketWrapper> {
    override fun deserialize(json: JsonElement, typeOfT: Type, context: JsonDeserializationContext): WebSocketWrapper {
        val jsonObject = json.asJsonObject
        return WebSocketWrapper(uuid = jsonObject.get("uuid").asString,
                isBinary = jsonObject.get("is_binary").asBoolean,
                timeStamp = jsonObject.get("timestamp").asLong,
                msgFrom = jsonObject.get("msg_from").asInt,
                msgType = fromInt(jsonObject.get("msg_type").asInt),
                payload = jsonObject.get("payload").asString)
    }
}

class MessageTypeSerializer : JsonSerializer<MessageType> {
    override fun serialize(src: MessageType, typeOfSrc: Type, context: JsonSerializationContext): JsonElement {
        return JsonPrimitive(src.index)
    }
}

@JsonAdapter(WebSocketWrapperDeserializer::class)
data class WebSocketWrapper(val uuid: String,
                            @SerializedName("is_binary") val isBinary: Boolean,
                            @SerializedName("timestamp") val timeStamp: Long,
                            @SerializedName("msg_from") val msgFrom: Int,
                            @JsonAdapter(MessageTypeSerializer::class)
                            @SerializedName("msg_type") val msgType: MessageType,
                            val payload: String)