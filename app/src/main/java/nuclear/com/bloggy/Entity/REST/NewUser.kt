package nuclear.com.bloggy.Entity.REST

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.annotations.SerializedName

/**
 * Created by torri on 2017/12/11.
 */
data class NewUser(val email: String,
                   @SerializedName("password") val password: String,
                   @SerializedName("username") val username: String,
                   @SerializedName("about_me") val aboutMe: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readString(),
            parcel.readString(),
            parcel.readString())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(email)
        parcel.writeString(password)
        parcel.writeString(username)
        parcel.writeString(aboutMe)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewUser> {
        override fun createFromParcel(parcel: Parcel): NewUser {
            return NewUser(parcel)
        }

        override fun newArray(size: Int): Array<NewUser?> {
            return arrayOfNulls(size)
        }
    }

}