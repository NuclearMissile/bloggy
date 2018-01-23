package nuclear.com.bloggy.Entity

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class FollowState constructor(
        // i isFollowing
        @SerializedName("is_following") var isFollowing: Boolean,
        // i isFollowedBy
        @SerializedName("is_followed_by") var isFollowedBy: Boolean
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readByte() != 0.toByte(),
            parcel.readByte() != 0.toByte())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeByte(if (isFollowing) 1 else 0)
        parcel.writeByte(if (isFollowedBy) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FollowState> {
        override fun createFromParcel(parcel: Parcel): FollowState {
            return FollowState(parcel)
        }

        override fun newArray(size: Int): Array<FollowState?> {
            return arrayOfNulls(size)
        }
    }
}

