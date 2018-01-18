package nuclear.com.bloggy.Entity

import android.os.Parcel
import android.os.Parcelable
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import nuclear.com.bloggy.Util.DateUtil

@Entity
data class NewArticle(var body: String, @Id var id: Long = 0, var timeStamp: Long = DateUtil.TimeStamp) : Parcelable {
    constructor(parcel: Parcel) : this(
            parcel.readString(),
            parcel.readLong(),
            parcel.readLong())

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(body)
        parcel.writeLong(id)
        parcel.writeLong(timeStamp)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<NewArticle> {
        override fun createFromParcel(parcel: Parcel): NewArticle {
            return NewArticle(parcel)
        }

        override fun newArray(size: Int): Array<NewArticle?> {
            return arrayOfNulls(size)
        }
    }
}