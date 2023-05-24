package edu.licenta.uptconnect.model

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import androidx.annotation.RequiresApi
import com.google.firebase.firestore.DocumentId

data class Course(
    @DocumentId
    val id: String,
    val Name: String,
    val Section: String,
    val Year: String,
    val Mandatory: Boolean,
    val Examination: String,
    val teachingWay: Any
) : Parcelable {
    constructor() : this("", "", "", "", false, "", "")

    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readString()!!,
        parcel.readByte() != 0.toByte(),
        parcel.readString()!!,
        parcel.readValue(Any::class.java.classLoader)!!
    )

    override fun describeContents(): Int {
        return 0
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeString(Name)
        dest.writeString(Section)
        dest.writeString(Year)
        dest.writeBoolean(Mandatory)
        dest.writeString(Examination)
        dest.writeValue(teachingWay)
    }

    companion object CREATOR : Parcelable.Creator<Course> {
        override fun createFromParcel(parcel: Parcel): Course {
            return Course(parcel)
        }

        override fun newArray(size: Int): Array<Course?> {
            return arrayOfNulls(size)
        }
    }
}