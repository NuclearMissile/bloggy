package nuclear.com.bloggy.Entity.REST

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

data class Token constructor(
        val token: String,
        @SerializedName("expire_at") val expireAt: Long) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
        parcel.writeLong(expireAt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Token> {
        override fun createFromParcel(parcel: Parcel): Token {
            return Token(parcel)
        }

        override fun newArray(size: Int): Array<Token?> {
            return arrayOfNulls(size)
        }
    }
}