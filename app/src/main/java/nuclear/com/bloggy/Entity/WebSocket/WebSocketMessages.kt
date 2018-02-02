package nuclear.com.bloggy.Entity.WebSocket

import com.google.gson.annotations.SerializedName

data class PostAddedMessage(val msg: String,
                            @SerializedName("post_id") val postId: Int)

data class CommentAddedMessage(val msg: String,
                               @SerializedName("post_id") val postId: Int,
                               @SerializedName("comment_id") val commentId: Int)

data class BeFollowedMessage(val msg: String,
                             @SerializedName("follower_id") val followerId: Int)