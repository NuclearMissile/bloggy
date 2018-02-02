package nuclear.com.bloggy.Entity.REST

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class User constructor(val id: Int, val username: String, val email: String,
                            @SerializedName("member_since") val memberSince: Long,
                            @SerializedName("last_seen") val lastSeen: Long,
                            @SerializedName("about_me") val aboutMe: String?,
                            @SerializedName("followers_count") val followersCount: Int,
                            @SerializedName("followeds_count") val followedsCount: Int,
                            @SerializedName("avatar_hash") val avatarHash: String,
                            @SerializedName("posts_count") val postsCount: Int,
                            @SerializedName("user_link") val userLink: String,
                            val permissions: Int, val confirmed: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readString(),
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readString(),
            parcel.readInt(),
            parcel.readByte() != 0.toByte()) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(username)
        parcel.writeString(email)
        parcel.writeLong(memberSince)
        parcel.writeLong(lastSeen)
        parcel.writeString(aboutMe)
        parcel.writeInt(followersCount)
        parcel.writeInt(followedsCount)
        parcel.writeString(avatarHash)
        parcel.writeInt(postsCount)
        parcel.writeString(userLink)
        parcel.writeInt(permissions)
        parcel.writeByte(if (confirmed) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}