package nuclear.com.bloggy.Entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Comment constructor(
        val id: Int, val body: String,
        @SerializedName("post_id") val postId: Int,
        @SerializedName("author_id") val authorId: Int,
        @SerializedName("body_html") val htmlBody: String,
        @SerializedName("timestamp") val timeStamp: Long,
        @SerializedName("avatar_hash") val avatarHash: String,
        @SerializedName("author_name") val authorName: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readString()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(body)
        parcel.writeInt(postId)
        parcel.writeInt(authorId)
        parcel.writeString(htmlBody)
        parcel.writeLong(timeStamp)
        parcel.writeString(avatarHash)
        parcel.writeString(authorName)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Comment> {
        override fun createFromParcel(parcel: Parcel): Comment {
            return Comment(parcel)
        }

        override fun newArray(size: Int): Array<Comment?> {
            return arrayOfNulls(size)
        }
    }
}