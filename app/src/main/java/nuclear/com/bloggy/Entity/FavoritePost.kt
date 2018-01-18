package nuclear.com.bloggy.Entity

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id

@Entity
data class FavoritePost(val postId: Int, @Id var id: Long = 0) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readInt(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(postId)
        parcel.writeLong(id)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FavoritePost> {
        override fun createFromParcel(parcel: Parcel): FavoritePost {
            return FavoritePost(parcel)
        }

        override fun newArray(size: Int): Array<FavoritePost?> {
            return arrayOfNulls(size)
        }
    }
}