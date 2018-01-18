package nuclear.com.bloggy.Entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by torri on 2017/12/8.
 */
data class Post constructor(
        val id: Int, val body: String,
        @SerializedName("body_html") val htmlBody: String,
        @SerializedName("timestamp") val timeStamp: Long,
        @SerializedName("author_id") val authorId: Int,
        @SerializedName("comments_count") val commentsCount: Int,
        @SerializedName("author_avatar_hash") val authorAvatarHash: String,
        @SerializedName("author_name") val authorName: String,
        @SerializedName("post_link") val link: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(body)
        parcel.writeString(htmlBody)
        parcel.writeLong(timeStamp)
        parcel.writeInt(authorId)
        parcel.writeInt(commentsCount)
        parcel.writeString(authorAvatarHash)
        parcel.writeString(authorName)
        parcel.writeString(link)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Post> {
        override fun createFromParcel(parcel: Parcel): Post {
            return Post(parcel)
        }

        override fun newArray(size: Int): Array<Post?> {
            return arrayOfNulls(size)
        }
    }
}
