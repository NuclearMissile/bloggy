package nuclear.com.bloggy.Entity.WebSocket

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class PostAddedMessage(val msg: String,
                            @SerializedName("post_id") val postId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(msg)
        parcel.writeInt(postId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PostAddedMessage> {
        override fun createFromParcel(parcel: Parcel): PostAddedMessage {
            return PostAddedMessage(parcel)
        }

        override fun newArray(size: Int): Array<PostAddedMessage?> {
            return arrayOfNulls(size)
        }
    }
}

data class CommentAddedMessage(val msg: String,
                               @SerializedName("post_id") val postId: Int,
                               @SerializedName("comment_id") val commentId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(msg)
        parcel.writeInt(postId)
        parcel.writeInt(commentId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<CommentAddedMessage> {
        override fun createFromParcel(parcel: Parcel): CommentAddedMessage {
            return CommentAddedMessage(parcel)
        }

        override fun newArray(size: Int): Array<CommentAddedMessage?> {
            return arrayOfNulls(size)
        }
    }
}

data class BeFollowedMessage(val msg: String,
                             @SerializedName("follower_id") val followerId: Int) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readInt())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(msg)
        parcel.writeInt(followerId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<BeFollowedMessage> {
        override fun createFromParcel(parcel: Parcel): BeFollowedMessage {
            return BeFollowedMessage(parcel)
        }

        override fun newArray(size: Int): Array<BeFollowedMessage?> {
            return arrayOfNulls(size)
        }
    }
}